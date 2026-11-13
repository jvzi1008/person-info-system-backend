package com.ruoyi.compare.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizCompareAccessLog;
import com.ruoyi.compare.domain.dto.CompareQueryBody;
import com.ruoyi.compare.domain.vo.CompareResult;
import com.ruoyi.compare.domain.vo.StudentResultRow;
import com.ruoyi.compare.domain.vo.TransferRow;
import com.ruoyi.compare.exception.ExtAuthException;
import com.ruoyi.compare.mapper.BizCompareMapper;
import com.ruoyi.compare.service.IBizCompareAccessLogService;
import com.ruoyi.compare.service.ICompareService;

@Service
public class CompareServiceImpl implements ICompareService {

    private static final Set<String> POSITIVE_STATUS = new HashSet<>(Arrays.asList(
            "正常在校", "在读", "在校", "在籍在校"
    ));

    private static final Set<String> KNOWN_NEGATIVE_STATUS = new HashSet<>(Arrays.asList(
            "退学", "开除学籍", "毕业", "结业", "离校", "休学", "保留学籍"
    ));

    private static final String[] VOCATIONAL_KEYWORDS = {
            "职业", "中专", "技校", "技工", "职高", "职专",
            "卫生学校", "职业学院", "职业技术学院", "中等专业学校"
    };

    private static final String[] PRIMARY_SECONDARY_KEYWORDS = {
            "小学", "初中", "初级中学", "中学", "高级中学",
            "完全中学", "九年一贯", "中心小学", "附属中学"
    };

    @Autowired
    private BizCompareMapper bizCompareMapper;

    @Autowired
    private IBizCompareAccessLogService bizCompareAccessLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompareResult compare(CompareQueryBody body,
                                 Long userId,
                                 String loginName,
                                 String clientIp,
                                 String requestUri,
                                 String requestMethod,
                                 String userAgent) {
        long start = System.currentTimeMillis();

        String idNo = normalizeId(body.getIdCardNo());
        if (StringUtils.isEmpty(idNo)) {
            throw new ExtAuthException(400, "证件号不能为空");
        }
        if (idNo.length() > 64) {
            throw new ExtAuthException(400, "证件号长度不能超过64位");
        }

        String queryName = normalizeName(body.getName());
        if (queryName != null && queryName.length() > 100) {
            throw new ExtAuthException(400, "姓名长度不能超过100位");
        }

        List<StudentResultRow> studentRows = dedupStudents(
                bizCompareMapper.selectStudentResultByIdNo(idNo));
        List<TransferRow> transferRows = dedupTransfers(
                bizCompareMapper.selectTransferByIdNo(idNo));

        boolean hasPositive = false;
        boolean hasNegative = false;
        boolean unknownStatus = false;
        Set<String> statusSet = new LinkedHashSet<>();

        for (StudentResultRow row : studentRows) {
            String status = trim(row.getStatus());
            if (StringUtils.isNotEmpty(status)) {
                statusSet.add(status);
            }
            if (POSITIVE_STATUS.contains(status)) {
                hasPositive = true;
            } else if (KNOWN_NEGATIVE_STATUS.contains(status)) {
                hasNegative = true;
            } else {
                hasNegative = true;
                unknownStatus = true;
            }
        }

        boolean conflict = hasPositive && hasNegative;

        Set<String> nameSet = new LinkedHashSet<>();
        Set<String> schoolSet = new LinkedHashSet<>();

        for (StudentResultRow row : studentRows) {
            if (StringUtils.isNotEmpty(row.getName())) {
                nameSet.add(row.getName());
            }
            if (StringUtils.isNotEmpty(row.getSchoolName())) {
                schoolSet.add(row.getSchoolName());
            }
        }

        for (TransferRow row : transferRows) {
            if (StringUtils.isNotEmpty(row.getName())) {
                nameSet.add(row.getName());
            }
            addSchool(schoolSet, row.getFromSchool());
            addSchool(schoolSet, row.getToSchool());
        }

        boolean nameMismatch = StringUtils.isNotEmpty(queryName)
                && !nameSet.isEmpty()
                && !nameSet.contains(queryName);

        boolean vocational = false;
        boolean primarySecondary = false;

        for (String school : schoolSet) {
            if (isVocational(school)) {
                vocational = true;
            } else if (isPrimarySecondary(school)) {
                primarySecondary = true;
            }
        }

        String studentType;
        if (vocational && primarySecondary) {
            studentType = "BOTH";
        } else if (vocational) {
            studentType = "VOCATIONAL";
        } else if (primarySecondary) {
            studentType = "PRIMARY_SECONDARY";
        } else {
            studentType = "UNKNOWN";
        }

        String targetStudentStatus;
        if (schoolSet.isEmpty()) {
            targetStudentStatus = "UNKNOWN";
        } else if (vocational || primarySecondary) {
            targetStudentStatus = "YES";
        } else {
            targetStudentStatus = "NO";
        }

        boolean hasTransfer = !transferRows.isEmpty();
        boolean noRows = studentRows.isEmpty() && transferRows.isEmpty();

        String queryStatus;
        if (noRows) {
            queryStatus = "NOT_FOUND";
        } else if (nameMismatch) {
            queryStatus = "NAME_MISMATCH";
        } else {
            queryStatus = "MATCHED";
        }

        String inSchoolStatus;
        if (studentRows.isEmpty()) {
            inSchoolStatus = "UNKNOWN";
        } else if (conflict) {
            inSchoolStatus = "CONFLICT";
        } else if (hasPositive) {
            inSchoolStatus = "YES";
        } else if (unknownStatus) {
            inSchoolStatus = "UNKNOWN";
        } else {
            inSchoolStatus = "NO";
        }

        List<String> reviewReasons = new ArrayList<>();
        if (!noRows) {
            if (studentRows.isEmpty() && !transferRows.isEmpty()) {
                reviewReasons.add("TRANSFER_ONLY");
            }
            if (conflict) {
                reviewReasons.add("STATUS_CONFLICT");
            }
            if (unknownStatus) {
                reviewReasons.add("UNKNOWN_STATUS");
            }
            if ("NO".equals(targetStudentStatus)) {
                reviewReasons.add("NON_TARGET_SCHOOL");
            }
        }

        boolean needReview = !"MATCHED".equals(queryStatus) || !reviewReasons.isEmpty();
        boolean infoProblem = !"MATCHED".equals(queryStatus);

        Boolean inSchool = "YES".equals(inSchoolStatus) ? Boolean.TRUE
                : "NO".equals(inSchoolStatus) ? Boolean.FALSE : null;
        Boolean targetStudent = "YES".equals(targetStudentStatus) ? Boolean.TRUE
                : "NO".equals(targetStudentStatus) ? Boolean.FALSE : null;

        int matchCount = studentRows.size() + transferRows.size();

        CompareResult result = new CompareResult();
        result.setIdCardNo(maskId(idNo));
        result.setName(StringUtils.isNotEmpty(queryName)
                ? queryName
                : (nameSet.isEmpty() ? null : nameSet.iterator().next()));
        result.setInSchool(inSchool);
        result.setTargetStudent(targetStudent);
        result.setHasTransfer(hasTransfer);
        result.setStudentType(studentType);
        result.setConflict(conflict);
        result.setNeedReview(needReview);
        result.setNameMismatch(nameMismatch);
        result.setInfoProblem(infoProblem);
        result.setQueryStatus(queryStatus);
        result.setReviewReasons(reviewReasons);
        result.setInSchoolStatus(inSchoolStatus);
        result.setTargetStudentStatus(targetStudentStatus);
        result.setMatchCount(matchCount);
        result.setStatusList(new ArrayList<>(statusSet));
        result.setStudentResults(studentRows);
        result.setTransfers(transferRows);

        String resultCode;
        String resultMsg;
        if ("NOT_FOUND".equals(queryStatus)) {
            resultCode = "NO_MATCH";
            resultMsg = "未查询到该身份证号对应的人员信息，请核对身份证号和姓名";
        } else if ("NAME_MISMATCH".equals(queryStatus)) {
            resultCode = "NAME_MISMATCH";
            resultMsg = "姓名与系统记录不一致，请核对姓名";
        } else if (conflict) {
            resultCode = "CONFLICT";
            resultMsg = "存在在校状态冲突，建议人工复核";
        } else if (unknownStatus) {
            resultCode = "SUCCESS";
            resultMsg = "存在未知在校状态，建议人工复核";
        } else if (studentRows.isEmpty()) {
            resultCode = "TRANSFER_ONLY";
            resultMsg = "仅匹配到转学记录，未匹配到学生结果，建议人工复核";
        } else if ("NO".equals(targetStudentStatus)) {
            resultCode = "SUCCESS";
            resultMsg = "未识别到中小学生或职业学生学校，建议人工复核";
        } else if (needReview) {
            resultCode = "SUCCESS";
            resultMsg = "比对完成，部分数据需人工复核";
        } else {
            resultCode = "SUCCESS";
            resultMsg = "比对完成";
        }
        result.setResultCode(resultCode);
        result.setResultMsg(resultMsg);

        BizCompareAccessLog log = new BizCompareAccessLog();
        log.setUserId(userId);
        log.setLoginName(loginName);
        log.setIpAddress(clientIp);
        log.setRequestUri(truncate(requestUri, 255));
        log.setRequestMethod(truncate(requestMethod, 10));
        log.setUserAgent(truncate(userAgent, 500));
        log.setQueryName(truncate(queryName, 100));
        log.setQueryIdCard(maskId(idNo));
        log.setQueryIdCardHash(sha256(idNo));

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("idCardNo", maskId(idNo));
        queryParams.put("name", queryName);
        log.setQueryParams(toJson(queryParams));

        log.setMatchCount(matchCount);
        log.setIsInSchool(flagStatus(inSchoolStatus));
        log.setStudentType(studentType);
        log.setIsTargetStudent(flagStatus(targetStudentStatus));
        log.setHasTransfer(flag(hasTransfer));
        log.setConflictFlag(flag(conflict));
        log.setNeedReview(flag(needReview));
        log.setResultCode(resultCode);
        log.setResultMsg(resultMsg);
        log.setResultJson(toJson(result));
        log.setCostMs((int) (System.currentTimeMillis() - start));
        log.setRequestTime(new Date());
        bizCompareAccessLogService.insertBizCompareAccessLog(log);

        return result;
    }

    private List<StudentResultRow> dedupStudents(List<StudentResultRow> rows) {
        List<StudentResultRow> result = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return result;
        }
        Set<String> seen = new HashSet<>();
        for (StudentResultRow row : rows) {
            if (row == null) {
                continue;
            }
            row.setName(normalizeName(row.getName()));
            row.setStatus(trim(row.getStatus()));
            row.setSchoolName(trim(row.getSchoolName()));
            String key = safe(row.getName()) + "|" + safe(row.getStatus()) + "|" + safe(row.getSchoolName());
            if (seen.add(key)) {
                result.add(row);
            }
        }
        return result;
    }

    private List<TransferRow> dedupTransfers(List<TransferRow> rows) {
        List<TransferRow> result = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return result;
        }
        Set<String> seen = new HashSet<>();
        for (TransferRow row : rows) {
            if (row == null) {
                continue;
            }
            row.setName(normalizeName(row.getName()));
            row.setFromSchool(trim(row.getFromSchool()));
            row.setToSchool(trim(row.getToSchool()));
            String key = safe(row.getFromSchool()) + "|" + safe(row.getToSchool())
                    + "|" + safe(row.getZxlx()) + "|" + safe(row.getApplyTime());
            if (seen.add(key)) {
                result.add(row);
            }
        }
        return result;
    }

    private boolean isVocational(String schoolName) {
        if (StringUtils.isEmpty(schoolName)) {
            return false;
        }
        for (String keyword : VOCATIONAL_KEYWORDS) {
            if (schoolName.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPrimarySecondary(String schoolName) {
        if (StringUtils.isEmpty(schoolName)) {
            return false;
        }
        for (String keyword : PRIMARY_SECONDARY_KEYWORDS) {
            if (schoolName.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void addSchool(Set<String> schoolSet, String schoolName) {
        String school = trim(schoolName);
        if (StringUtils.isNotEmpty(school)) {
            schoolSet.add(school);
        }
    }

    private String normalizeId(String idCardNo) {
        if (StringUtils.isEmpty(idCardNo)) {
            return null;
        }
        return idCardNo.trim().toUpperCase();
    }

    private String normalizeName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return name.replace("\"", "").trim();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safe(Integer value) {
        return value == null ? "" : value.toString();
    }

    private String flag(boolean value) {
        return value ? "1" : "0";
    }

    private String flagStatus(String status) {
        if ("YES".equals(status)) {
            return "1";
        }
        if ("NO".equals(status)) {
            return "0";
        }
        return null;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String maskId(String idCardNo) {
        if (StringUtils.isEmpty(idCardNo)) {
            return null;
        }
        if (idCardNo.length() <= 3) {
            return "****";
        }
        if (idCardNo.length() <= 10) {
            return idCardNo.charAt(0) + "****" + idCardNo.substring(idCardNo.length() - 2);
        }
        return idCardNo.substring(0, 6) + "****" + idCardNo.substring(idCardNo.length() - 4);
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return "{}";
        }
    }
}
