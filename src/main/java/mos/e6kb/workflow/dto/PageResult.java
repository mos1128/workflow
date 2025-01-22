package mos.e6kb.workflow.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 * @author ly
 * @since 2025/1/21
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 8656597559014685635L;


    /**
     * 结果集
     */
    private List<T> data;

    /**
     * 总记录数
     */
    private long totalCount;

    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private long totalPage;

    public PageResult(Integer pageNo, Integer pageSize, List<T> data, long totalCount) {
        this.data = data;
        this.totalCount = totalCount;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        //计算总页数
        this.totalPage = (totalCount + pageSize - 1) / pageSize;
    }

}
