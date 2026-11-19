package com.ruoyi.compare.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizIpAuth extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String ipAddress;
    private String sourceType;
    private String authType;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    private Integer maxQps;

    private String auditStatus;
    private String auditBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date auditTime;

    private String auditRemark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getEffectiveTime() { return effectiveTime; }
    public void setEffectiveTime(Date effectiveTime) { this.effectiveTime = effectiveTime; }

    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }

    public Integer getMaxQps() { return maxQps; }
    public void setMaxQps(Integer maxQps) { this.maxQps = maxQps; }

    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }

    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }

    public java.util.Date getAuditTime() { return auditTime; }
    public void setAuditTime(java.util.Date auditTime) { this.auditTime = auditTime; }

    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }

    @Override
    public String toString() {
        return "BizIpAuth{" +
                "id=" + id +
                ", userId=" + userId +
                ", ipAddress='" + ipAddress + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", authType='" + authType + '\'' +
                ", status='" + status + '\'' +
                ", maxQps=" + maxQps +
                '}';
    }
}
