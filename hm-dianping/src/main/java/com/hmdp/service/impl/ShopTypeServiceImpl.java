package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.util.internal.StringUtil;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.RedisConstants.LOCK_SHOP_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /*
     * 商户类型列表
     * */
    @Override
    public Result queryListByAsc() {
        //1.从Redis里获取商户缓存
        String shopTypeJson = stringRedisTemplate.opsForList().rightPop(LOCK_SHOP_KEY);
        //2.判断是否存在，存在直接返回
        if (StrUtil.isNotBlank(shopTypeJson)){
                //3.存在直接返回
                /*// Step1: 解析JSON字符串为JSONArray
                JSONArray jsonArray = JSONUtil.parseArray(shopTypeJson);
                // Step2: 转换为List<Shop>
                List<Shop> shopList = jsonArray.toList(Shop.class);*/
            return Result.ok(JSONUtil.toList(JSONUtil.parseArray(shopTypeJson), ShopType.class));
        }
        //4.不存在，查询数据库
        List<ShopType> shopTypeList = query().orderByAsc("sort").list();
        //5.不存在，返回错误
        if (shopTypeList == null){
            return Result.fail("商户类型列表不存在!");
        }
        //6.存在，写入redis
        stringRedisTemplate.opsForList().leftPush(LOCK_SHOP_KEY,JSONUtil.toJsonStr(shopTypeList));
        //7.返回
        return Result.ok(shopTypeList);
    }
}
