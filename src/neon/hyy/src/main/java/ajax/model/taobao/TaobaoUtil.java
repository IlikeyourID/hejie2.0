package ajax.model.taobao;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AtbItemsDetailGetRequest;
import com.taobao.api.request.TbkUatmFavoritesGetRequest;
import com.taobao.api.request.TbkUatmFavoritesItemGetRequest;
import com.taobao.api.response.AtbItemsDetailGetResponse;
import com.taobao.api.response.TbkUatmFavoritesGetResponse;
import com.taobao.api.response.TbkUatmFavoritesItemGetResponse;

import ajax.model.exception.AJRunTimeException;
import ajax.model.taobao.model.GoodsType;
import ajax.model.taobao.model.ITaobao;
import ajax.model.taobao.model.ITaobaoItemQueryParams;
import ajax.model.taobao.model.Platform;
import ajax.model.taobao.model.TbkItem;
import ajax.model.taobao.model.TbkItemPC;
import ajax.model.taobao.model.TbkItemWap;
import ajax.model.taobao.model.XuanPinKu;


public class TaobaoUtil {
	private static String url = Taobao.url;
	private static String appkey = Taobao.getTAOBAO_NIGEERHUO388_APP_KEY();
	private static String secret = Taobao.getTAOBAO_NIGEERHUO388_APP_SECRET();
	/**
	 * items推广位的adzoneid
	 */
	private static final Long AdZONE_Id_OF_ITEMS = 63604146L;
	
	private class A{
		public B tbk_uatm_favorites_get_response;
	}
	private class B {
		public C results;
	}
	private class C{
		public List<XuanPinKu> tbk_favorites;
	}
	/**
	 * @return return null if exception occurs
	 */
	public static String taobaoTbkUatmFavoritesGet() {
		TaobaoClient client = new DefaultTaobaoClient(Taobao.url, Taobao.getTAOBAO_NIGEERHUO388_APP_KEY(), Taobao.getTAOBAO_NIGEERHUO388_APP_SECRET());
		TbkUatmFavoritesGetRequest req = new TbkUatmFavoritesGetRequest();
		req.setPageNo(1L);
		req.setPageSize(100L);
		req.setFields("favorites_title,favorites_id,type");
		req.setType(1L);
		TbkUatmFavoritesGetResponse rsp;
		try {
			rsp = (TbkUatmFavoritesGetResponse) client.execute(req);
			return rsp.getBody();
		} catch (ApiException e) {
			return null;
		}
	}
	
	/**
	 * null if (exception or json == null)
	 * @return
	 */
	public static List<XuanPinKu> getXPKList(){
		String json = taobaoTbkUatmFavoritesGet();
		if (json == null) {
			return new ArrayList<>();
		} else {
			Gson gson = new Gson();
			A a = gson.fromJson(json, A.class);
			try {
				return a.tbk_uatm_favorites_get_response.results.tbk_favorites;
			} catch(Exception ex) {
				return new ArrayList<>();
			}
		}
	}
	
	/**
	 * (获取淘宝联盟选品库的宝贝信息)
	 * @return null if exception occurs
	 */
	public static String taobaoTbkUatmFavoritesItemGet(Long adZoneId, Long xpkId, Platform platform) {
		TaobaoClient client = new DefaultTaobaoClient(url, appkey, secret);
		TbkUatmFavoritesItemGetRequest req = new TbkUatmFavoritesItemGetRequest();
		//链接形式：1：PC，2：无线，默认：１
		req.setPlatform(platform.getId());
		req.setPageSize(100L);
		req.setAdzoneId(adZoneId);
		req.setFavoritesId(xpkId);
		//	第几页 默认 1
		//req.setPageNo(2L);
		req.setFields("num_iid,title,pict_url,small_images,reserve_price,zk_final_price,user_type,click_url,provcity,item_url,seller_id,volume,nick,shop_title,zk_final_price_wap,event_start_time,event_end_time,tk_rate,status,type");
		TbkUatmFavoritesItemGetResponse rsp;
		try {
			rsp = client.execute(req);
		} catch (ApiException e) {
			return null;
		}
		return rsp.getBody();
	}
	
	private interface ReturnTbkItems<T>{
		List<T> returnTbkItems();
	}
	
	private class D_PC implements ReturnTbkItems<TbkItemPC>{
		E_PC tbk_uatm_favorites_item_get_response;

		@Override
		public List<TbkItemPC> returnTbkItems() {
			return this.tbk_uatm_favorites_item_get_response.results.uatm_tbk_item;
		}
	}
	private class E_PC{
		F_PC results;
	}
	private class F_PC{
		List<TbkItemPC> uatm_tbk_item;
	}
	
	
	private class D_Wap implements ReturnTbkItems<TbkItemWap>{
		E_Wap tbk_uatm_favorites_item_get_response;
		
		@Override
		public List<TbkItemWap> returnTbkItems() {
			return this.tbk_uatm_favorites_item_get_response.results.uatm_tbk_item;
		}
	}
	private class E_Wap{
		F_Wap results;
	}
	private class F_Wap{
		List<TbkItemWap> uatm_tbk_item;
	}
	/**
	 * 获取items推广位的某个选品库的tbkitem列表<br>
	 * 注意千万不要弄错, 推广位弄错会不计算收益!!<br>
	 * 默认PC platform
	 * return empty list if exception occurs
	 * @param xuanPinKu
	 * @return
	 * @throws AJRunTimeException 
	 */
	public static List<TbkItem> getTbkItemsFromXPKInItemsTuikuangwei(XuanPinKu xuanPinKu) throws AJRunTimeException {
		return getTbkItemsFromXPKInItemsTuikuangwei(xuanPinKu, Platform.PC);
	}
	/**
	 * 获取items推广位的某个选品库的tbkitem列表<br>
	 * 注意千万不要弄错, 推广位弄错会不计算收益!!
	 * return empty list if exception occurs
	 * @param xuanPinKu
	 * @return
	 * @throws AJRunTimeException 
	 */
	public static List<TbkItem> getTbkItemsFromXPKInItemsTuikuangwei(XuanPinKu xuanPinKu, Platform platform) throws AJRunTimeException {
		String res = TaobaoUtil.taobaoTbkUatmFavoritesItemGet(AdZONE_Id_OF_ITEMS, xuanPinKu.getFavorites_id(), platform);
		
		System.out.println(res);
		
		ReturnTbkItems returnTbkItems = null;
		switch(platform){
		case PC:
			returnTbkItems = new Gson().fromJson(res, D_PC.class);
			break;
		case WAP:
			returnTbkItems = new Gson().fromJson(res, D_Wap.class);
			break;
		default :
			throw new AJRunTimeException("no given platform");
			
		}
		
		
		try {
			return returnTbkItems.returnTbkItems();
		} catch (Exception ex) {
			return new ArrayList<>();
		}
	}
	
	/**
	 * 从taobao
	 * 1.获取选品库列表
	 * 2.获取每个选品库对应的tbkitem, 并标记类目
	 * 3.保存所有tbkitem到数据库(PC和wap)
	 * @throws AJRunTimeException
	 */
	public static void grabTbkItemsFromXuanpinkuAndSaveToPcAndWapTable() throws AJRunTimeException {
		List<XuanPinKu> xuanPinKus = TaobaoUtil.getXPKList();
		
		for (XuanPinKu xuanPinKu : xuanPinKus) {
			
			if (xuanPinKu.isGrab()) continue;
			xuanPinKu.setIsGrab();
			
			GoodsType goodsType = xuanPinKu.returnGoodsType();
			
			List<TbkItem> tbkItemPCs = TaobaoUtil.getTbkItemsFromXPKInItemsTuikuangwei(xuanPinKu, Platform.PC);
			List<TbkItem> tbkItemWaps = TaobaoUtil.getTbkItemsFromXPKInItemsTuikuangwei(xuanPinKu, Platform.WAP);
			
			for (TbkItem tbkItem : tbkItemPCs) {
				tbkItem.setGoodsTypeId(goodsType.getId());
			}
			
			for (TbkItem tbkItem : tbkItemWaps) {
				tbkItem.setGoodsTypeId(goodsType.getId());
			}
			
			for (TbkItem item : tbkItemPCs) {
				item.save();
			}
			
			for (TbkItem item : tbkItemWaps) {
				item.save();
			}
		}
		System.out.println("OVER!!!!!!!!!!");
	}
	
	public List<ITaobao> getITaobaoByKeyWords() throws JsonSyntaxException, ApiException {
		TbkItem tbkItem = TbkItemPC.get(TbkItemPC.class, 115L);
		
		ITaobaoItemQueryParams iTaobaoItemQueryParams = new ITaobaoItemQueryParams();
		iTaobaoItemQueryParams.keyword = tbkItem.getTitle();
		List<ITaobao> iTaobaos = Taobao.getITaobaoItems(iTaobaoItemQueryParams);
		
		for (ITaobao iTaobao : iTaobaos) {
			iTaobao.grabDetail();
		}
		return iTaobaos;
	}
	
	public static void main(String[] args) throws AJRunTimeException {
		TaobaoUtil.grabTbkItemsFromXuanpinkuAndSaveToPcAndWapTable();
	}
	
}
