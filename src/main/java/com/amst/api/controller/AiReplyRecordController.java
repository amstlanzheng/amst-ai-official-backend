package com.amst.api.controller;

import com.amst.api.service.AiReplyRecordService;
import com.mybatisflex.core.paginate.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.amst.api.model.entity.AiReplyRecord;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * AI 回复内容记录 控制层。
 *
 * @author lanzhs
 */
@RestController
@RequestMapping("/aiReplyRecord")
public class AiReplyRecordController {

    @Autowired
    private AiReplyRecordService aiReplyRecordService;

    /**
     * 保存AI 回复内容记录。
     *
     * @param aiReplyRecord AI 回复内容记录
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody AiReplyRecord aiReplyRecord) {
        return aiReplyRecordService.save(aiReplyRecord);
    }

    /**
     * 根据主键删除AI 回复内容记录。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return aiReplyRecordService.removeById(id);
    }

    /**
     * 根据主键更新AI 回复内容记录。
     *
     * @param aiReplyRecord AI 回复内容记录
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody AiReplyRecord aiReplyRecord) {
        return aiReplyRecordService.updateById(aiReplyRecord);
    }

    /**
     * 查询所有AI 回复内容记录。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<AiReplyRecord> list() {
        return aiReplyRecordService.list();
    }

    /**
     * 根据主键获取AI 回复内容记录。
     *
     * @param id AI 回复内容记录主键
     * @return AI 回复内容记录详情
     */
    @GetMapping("getInfo/{id}")
    public AiReplyRecord getInfo(@PathVariable Long id) {
        return aiReplyRecordService.getById(id);
    }

    /**
     * 分页查询AI 回复内容记录。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<AiReplyRecord> page(Page<AiReplyRecord> page) {
        return aiReplyRecordService.page(page);
    }

}
