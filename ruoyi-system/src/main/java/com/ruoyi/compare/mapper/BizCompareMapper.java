package com.ruoyi.compare.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.compare.domain.vo.StudentResultRow;
import com.ruoyi.compare.domain.vo.TransferRow;

public interface BizCompareMapper {

    List<StudentResultRow> selectStudentResultByIdNo(@Param("idNo") String idNo);

    List<TransferRow> selectTransferByIdNo(@Param("idNo") String idNo);
}
