package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {
    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsRepository repository;

    @Autowired
    private ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu){
        Long spuId = spu.getId();
        //????????????
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        //????????????
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //???????????? all
        String all =spu.getSubTitle()+ StringUtils.join(names," ")+brand.getName();

        //??????sku
        List<Sku> skus = goodsClient.querySkusBySpuId(spuId);
        //???sku????????????
        List<Map<String,Object>> skuList = new ArrayList<>();
        //????????????
        Set<Long> priceSet= new HashSet<>();
        for (Sku sku : skus) {
            Map<String,Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));
            skuList.add(map);
            //????????????
            priceSet.add(sku.getPrice());
        }

        //????????????????????????
        List<SpecParam> params = specificationClient.queryParamList(spu.getCid3(), null, true);
        //??????????????????
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spuId);
        //??????????????????????????????
        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //??????????????????????????????
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long,List<String>>>() {});

        //???????????????key????????????????????????value?????????????????????
        Map<String,Object> specs= new HashMap<>();
        for (SpecParam param : params) {
            //????????????
            String key = param.getName();
            //????????????
            Object value="";
            //???????????????????????????
            if (param.getGeneric()){
                value=genericSpec.get(param.getId());
                //???????????????????????????
                if (param.getNumeric()){
                    //????????????
                    value = chooseSegment(value.toString(), param);
                }

            }else {
                value=specialSpec.get(param.getId());
            }
            //??????map
            specs.put(key,value);
        }

        Goods goods = new Goods();
        //??????goods??????
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spuId);
        goods.setSubTitle(spu.getSubTitle());
        goods.setAll(all); //  ?????????????????????????????????????????????????????????
        goods.setPrice(priceSet); // ??????sku???????????????
        goods.setSkus(JsonUtils.toString(skuList)); //  ??????sku?????????json??????
        goods.setSpecs(specs); //  ??????????????????????????????


        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "??????";
        // ???????????????
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // ??????????????????
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // ????????????????????????
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "??????";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "??????";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest request) {
        int page = request.getPage() - 1; //??????queryBuilder.withPageable(PageRequest.of(page,size));????????????0??????
        int size = request.getSize();
        //?????????????????????
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //????????????
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //??????
        queryBuilder.withPageable(PageRequest.of(page,size));
        //??????????????????,all?????????index??????Field
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withFilter(basicQuery);

        //?????????????????????
        //????????????
        String categoryAggName="category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //????????????
        String brandAggName="brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));


        //??????
        //Page<Goods> result = repository.search(queryBuilder.build());
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

        //????????????
        //??????????????????
        int totalPage = result.getTotalPages();
        long total = result.getTotalElements();
        List<Goods> goodsList = result.getContent();
        //??????????????????
        Aggregations aggs = result.getAggregations();
        List<Brand> brands = parseBrandAgg(aggs.get(brandAggName));
        List<Category> categories = parseCategoryAgg(aggs.get(categoryAggName));

        //????????????????????????
        List<Map<String,Object>> specs=null;
        if (categories !=null && categories.size()==1){
            //?????????????????????????????????1,????????????????????????
            specs=buildSpecificationAgg(categories.get(0).getId(),basicQuery);
        }

        return new SearchResult(total,totalPage,goodsList,categories,brands,specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        // ??????????????????
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // ????????????
        queryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()));
        // ????????????
        Map<String, String> map = request.getFilter();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            //??????key
            if (!"cid3".equals(key) && !"brandId".equals(key)){
                key="specs." + key + ".keyword";
            }
            queryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));

        }
        return queryBuilder;
    }

    private List<Map<String,Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String,Object>> specs = new ArrayList<>();
        //1????????????????????????????????????
        List<SpecParam> params = specificationClient.queryParamList(cid, null, true);
        //2?????????
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //2.1????????????????????????
        queryBuilder.withQuery(basicQuery);
        //2.2?????????
        for (SpecParam param : params) {
            String name =param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        //3???????????????
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //4???????????????
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : params) {
            //??????????????????
            String name = param.getName();
            StringTerms terms = aggs.get(name);
            //?????????
            List<String> options = terms.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList());
            //??????map
            Map<String,Object> map = new HashMap<>();
            map.put("k",name);
            map.put("options",options);
            specs.add(map);
        }
        return specs;
    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(ids);
            return brands;
        } catch (Exception e) {
            log.error("????????????????????????????????????",e);
            return null;
        }
    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
            return categories;
        } catch (Exception e) {
            log.error("????????????????????????????????????",e);
            return null;
        }
    }

    public void createOrUpdateIndex(Long spuId) {
        //??????spu
        Spu spu = goodsClient.querySpuById(spuId);
        //??????goods
        Goods goods = buildGoods(spu);
        //???????????????
        repository.save(goods);

    }

    public void deleteIndex(Long spuId) {
        repository.deleteById(spuId);
    }
}
