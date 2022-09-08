package com.chl.victory.dao.manager;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.chl.victory.dao.exception.DaoManagerException;
import com.chl.victory.dao.mapper.BaseMapper;
import com.chl.victory.dao.model.BaseDO;
import com.chl.victory.dao.model.StatusCountDO;
import com.chl.victory.dao.query.BaseQuery;

/**
 * base数据层访问入口，适用于未使用tk.mybatis时mbg生成的mapper
 * <ul>
 * <li>
 * <br/><b>每个manager代表一个数据域操作，要求manager内只操作直接相关的表，不调用其他manager，如需域间验证请在业务service层完成</b>
 * </li>
 * <li>
 * <br/>定义了表ID的生成规则
 * </li>
 * <li>
 * <br/>crud的通用流程，建议子类对每个表分别封装crud行为，这样业务service可以灵活制定流程
 * </li>
 * <li>
 * <br/>其与子类不保证事务，需要业务service层增加事务
 * </li>
 * </ul>
 * @author ChenHailong
 * @date 2019/4/30 16:48
 **/
public class BaseManager4Mybatis extends BaseManager {

    /**
     * @param mapper
     * @param model 待保存数据模型
     * @param checkNotExist4Insert insert时，not null则排重验证，null时则直接insert
     * @param checkOnlyOne4Update update时，must not null, 验证存在单条记录
     * @return 影响记录数
     * @throws DaoManagerException
     */
    protected int save(BaseMapper mapper, BaseDO model, BaseQuery checkNotExist4Insert, BaseQuery checkOnlyOne4Update,
            TableNameEnum tableNameEnum) throws DaoManagerException {
        try {
            if (model.getId() == null) {
                // 插入前,排重
                if (null != checkNotExist4Insert) {
                    int count = mapper.count(checkNotExist4Insert);
                    if (count > 0) {
                        throw new DaoManagerException("已经存在相同记录，" + JSON.toJSON(checkNotExist4Insert));
                    }
                }

                if (model.getCreatedTime() == null) {
                    model.setCreatedTime(new Date());
                }
                if (model.getModifiedTime() == null) {
                    model.setModifiedTime(new Date());
                }

                // 插入新记录
                model.setId(generateId(tableNameEnum));
                return mapper.insert(model);
            }

            // 更新记录前，验证影响记录数为1
            if (checkOnlyOne4Update != null) {
                int count = mapper.count(checkOnlyOne4Update);
                if (count > 1) {
                    throw new DaoManagerException("待更新记录数不能大于1，" + checkOnlyOne4Update);
                }
                else if (count == 1) {
                    return mapper.update(model);
                }
            }
        } catch (Exception e) {
            throw new DaoManagerException(e);
        }

        return 0;
    }

    protected int del(BaseMapper mapper, BaseQuery query) throws DaoManagerException {
        try {
            return mapper.delete(query);
        } catch (Exception e) {
            throw new DaoManagerException(e);
        }
    }

    protected int count(BaseMapper mapper, BaseQuery query) throws DaoManagerException {
        try {
            return mapper.count(query);
        } catch (Exception e) {
            throw new DaoManagerException(e);
        }
    }

    protected <D> List<D> select(BaseMapper mapper, BaseQuery query) throws DaoManagerException {
        try {
            if (query.isJustOutline()) {
                return mapper.selectOutline(query);
            }
            return mapper.select(query);
        } catch (Exception e) {
            throw new DaoManagerException(e);
        }
    }

    /**
     * 当要求db仅能匹配到一条记录时使用
     * @param mapper
     * @param query
     * @param <D>
     * @return
     * @throws DaoManagerException
     */
    protected <D> D selectOne(BaseMapper mapper, BaseQuery query) throws DaoManagerException {
        try {
            return (D) mapper.selectOne(query);
        } catch (Exception e) {
            throw new DaoManagerException(e);
        }
    }

    public List<StatusCountDO> countStatus(BaseMapper mapper, BaseQuery query) {
        return mapper.countStatus(query);
    }
}
