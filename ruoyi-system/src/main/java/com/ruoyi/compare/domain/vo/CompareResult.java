package com.ruoyi.compare.domain.vo;

import java.util.List;

public class CompareResult {

    private String idCardNo;
    private String name;

    private Boolean inSchool;
    private Boolean targetStudent;
    private Boolean hasTransfer;

    private String studentType;
    private Boolean conflict;
    private Boolean needReview;
    private Boolean nameMismatch;
    private Boolean infoProblem;

    private String queryStatus;
    private List<String> reviewReasons;
    private String inSchoolStatus;
    private String targetStudentStatus;

    private Integer matchCount;
    private List<String> statusList;
    private List<StudentResultRow> studentResults;
    private List<TransferRow> transfers;

    private String resultCode;
    private String resultMsg;

    public String getIdCardNo() { return idCardNo; }
    public void setIdCardNo(String idCardNo) { this.idCardNo = idCardNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getInSchool() { return inSchool; }
    public void setInSchool(Boolean inSchool) { this.inSchool = inSchool; }

    public Boolean getTargetStudent() { return targetStudent; }
    public void setTargetStudent(Boolean targetStudent) { this.targetStudent = targetStudent; }

    public Boolean getHasTransfer() { return hasTransfer; }
    public void setHasTransfer(Boolean hasTransfer) { this.hasTransfer = hasTransfer; }

    public String getStudentType() { return studentType; }
    public void setStudentType(String studentType) { this.studentType = studentType; }

    public Boolean getConflict() { return conflict; }
    public void setConflict(Boolean conflict) { this.conflict = conflict; }

    public Boolean getNeedReview() { return needReview; }
    public void setNeedReview(Boolean needReview) { this.needReview = needReview; }

    public Boolean getNameMismatch() { return nameMismatch; }
    public void setNameMismatch(Boolean nameMismatch) { this.nameMismatch = nameMismatch; }

    public Boolean getInfoProblem() { return infoProblem; }
    public void setInfoProblem(Boolean infoProblem) { this.infoProblem = infoProblem; }

    public String getQueryStatus() { return queryStatus; }
    public void setQueryStatus(String queryStatus) { this.queryStatus = queryStatus; }

    public List<String> getReviewReasons() { return reviewReasons; }
    public void setReviewReasons(List<String> reviewReasons) { this.reviewReasons = reviewReasons; }

    public String getInSchoolStatus() { return inSchoolStatus; }
    public void setInSchoolStatus(String inSchoolStatus) { this.inSchoolStatus = inSchoolStatus; }

    public String getTargetStudentStatus() { return targetStudentStatus; }
    public void setTargetStudentStatus(String targetStudentStatus) { this.targetStudentStatus = targetStudentStatus; }

    public Integer getMatchCount() { return matchCount; }
    public void setMatchCount(Integer matchCount) { this.matchCount = matchCount; }

    public List<String> getStatusList() { return statusList; }
    public void setStatusList(List<String> statusList) { this.statusList = statusList; }

    public List<StudentResultRow> getStudentResults() { return studentResults; }
    public void setStudentResults(List<StudentResultRow> studentResults) { this.studentResults = studentResults; }

    public List<TransferRow> getTransfers() { return transfers; }
    public void setTransfers(List<TransferRow> transfers) { this.transfers = transfers; }

    public String getResultCode() { return resultCode; }
    public void setResultCode(String resultCode) { this.resultCode = resultCode; }

    public String getResultMsg() { return resultMsg; }
    public void setResultMsg(String resultMsg) { this.resultMsg = resultMsg; }
}
