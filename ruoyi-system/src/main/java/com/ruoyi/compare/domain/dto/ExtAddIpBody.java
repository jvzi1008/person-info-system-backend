package com.ruoyi.compare.domain.dto;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ExtAddIpBody {
    private String ipAddress;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date effectiveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    private Integer maxQps;
    private String remark;

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Date getEffectiveTime() { return effectiveTime; }
    public void setEffectiveTime(Date effectiveTime) { this.effectiveTime = effectiveTime; }

    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }

    public Integer getMaxQps() { return maxQps; }
    public void setMaxQps(Integer maxQps) { this.maxQps = maxQps; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
