package liuyuyang.net.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.jsonwebtoken.Claims;
import liuyuyang.net.properties.JwtProperties;
import liuyuyang.net.vo.FilterVo;
import liuyuyang.net.vo.PageVo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class YuYangUtils {
    @Resource
    private JwtProperties jwtProperties;

    // 分页查询逻辑
    public <T> Page<T> getPageData(PageVo pageVo, List<T> list) {
        int start = (pageVo.getPage() - 1) * pageVo.getSize();
        int end = Math.min(start + pageVo.getSize(), list.size());
        List<T> pagedRecords = list.subList(start, end);

        Page<T> result = new Page<>(pageVo.getPage(), pageVo.getSize());
        result.setRecords(pagedRecords);
        result.setTotal(list.size());

        return result;
    }

    // 过滤数据
    public <T> QueryWrapper<T> queryWrapperFilter(FilterVo filterVo) {
        return queryWrapperFilter(filterVo, "title");
    }

    public <T> QueryWrapper<T> queryWrapperFilter(FilterVo filterVo, String key) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");

        // 根据关键字通过标题过滤出对应数据
        if (filterVo.getKey() != null && !filterVo.getKey().isEmpty()) {
            queryWrapper.like(key, "%" + filterVo.getKey() + "%");
        }

        // 根据开始与结束时间过滤
        if (filterVo.getStartDate() != null && filterVo.getEndDate() != null) {
            queryWrapper.between("create_time", filterVo.getStartDate(), filterVo.getEndDate());
        } else if (filterVo.getStartDate() != null) {
            queryWrapper.ge("create_time", filterVo.getStartDate());
        } else if (filterVo.getEndDate() != null) {
            queryWrapper.le("create_time", filterVo.getEndDate());
        }

        return queryWrapper;
    }

    // 鉴权：判断是否为超级管理员
    public boolean isAdmin(String token) {
        if (token != null) {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            Claims claims = JwtUtils.parseJWT(jwtProperties.getSecretKey(), token);
            Map<String, Object> role = (Map<String, Object>) claims.get("role");

            // 是超级管理员
            return "admin".equals(role.get("mark"));
        }

        return false;
    }
}
