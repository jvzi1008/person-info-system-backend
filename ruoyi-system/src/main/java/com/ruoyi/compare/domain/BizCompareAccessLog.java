package com.ruoyi.compare.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizCompareAccessLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "日志编号")
    private Long id;
    private String traceId;
    @Excel(name = "用户ID")
    private Long userId;
    @Excel(name = "登录账号")
    private String loginName;
    @Excel(name = "请求IP")
    private String ipAddress;
    @Excel(name = "请求地址")
    private String requestUri;
    @Excel(name = "请求方式")
    private String requestMethod;
    private String userAgent;
    @Excel(name = "查询姓名")
    private String queryName;
    @Excel(name = "证件号")
    private String queryIdCard;
    private String queryIdCardHash;
    private String queryParams;
    @Excel(name = "命中条数")
    private Integer matchCount;
    @Excel(name = "是否在读", readConverterExp = "1=是,0=否")
    private String isInSchool;
    @Excel(name = "学生类型")
    private String studentType;
    @Excel(name = "中小学生或职业学生", readConverterExp = "1=是,0=否")
    private String isTargetStudent;
    @Excel(name = "是否有转学记录", readConverterExp = "1=是,0=否")
    private String hasTransfer;
    @Excel(name = "是否冲突", readConverterExp = "1=是,0=否")
    private String conflictFlag;
    @Excel(name = "是否需复核", readConverterExp = "1=是,0=否")
    private String needReview;
    @Excel(name = "结果码")
    private String resultCode;
    @Excel(name = "结果说明")
    private String resultMsg;
    private String resultJson;
    @Excel(name = "耗时(ms)")
    private Integer costMs;

    @Excel(name = "请求时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date requestTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getRequestUri() { return requestUri; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }

    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getQueryName() { return queryName; }
    public void setQueryName(String queryName) { this.queryName = queryName; }

    public String getQueryIdCard() { return queryIdCard; }
    public void setQueryIdCard(String queryIdCard) { this.queryIdCard = queryIdCard; }

    public String getQueryIdCardHash() { return queryIdCardHash; }
    public void setQueryIdCardHash(String queryIdCardHash) { this.queryIdCardHash = queryIdCardHash; }

    public String getQueryParams() { return queryParams; }
    public void setQueryParams(String queryParams) { this.queryParams = queryParams; }

    public Integer getMatchCount() { return matchCount; }
    public void setMatchCount(Integer matchCount) { this.matchCount = matchCount; }

    public String getIsInSchool() { return isInSchool; }
    public void setIsInSchool(String isInSchool) { this.isInSchool = isInSchool; }

    public String getStudentType() { return studentType; }
    public void setStudentType(String studentType) { this.studentType = studentType; }

    public String getIsTargetStudent() { return isTargetStudent; }
    public void setIsTargetStudent(String isTargetStudent) { this.isTargetStudent = isTargetStudent; }

    public String getHasTransfer() { return hasTransfer; }
    public void setHasTransfer(String hasTransfer) { this.hasTransfer = hasTransfer; }

    public String getConflictFlag() { return conflictFlag; }
    public void setConflictFlag(String conflictFlag) { this.conflictFlag = conflictFlag; }

    public String getNeedReview() { return needReview; }
    public void setNeedReview(String needReview) { this.needReview = needReview; }

    public String getResultCode() { return resultCode; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }

    public String getResultMsg() { return resultMsg; }
    public void setResultMsg(String resultMsg) { this.resultMsg = resultMsg; }

    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }

    public Integer getCostMs() { return costMs; }
    public void setCostMs(Integer costMs) { this.costMs = costMs; }

    public Date getRequestTime() { return requestTime; }
    public void setRequestTime(Date requestTime) { this.requestTime = requestTime; }
}
