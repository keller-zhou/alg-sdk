package com.slicejobs.algsdk.algtasklibrary.net;

import com.slicejobs.algsdk.algtasklibrary.model.OSSTicket;
import com.slicejobs.algsdk.algtasklibrary.model.Task;
import com.slicejobs.algsdk.algtasklibrary.net.response.LoginRes;
import com.slicejobs.algsdk.algtasklibrary.net.response.RegisterRes;
import com.slicejobs.algsdk.algtasklibrary.net.response.Response;
import com.slicejobs.algsdk.algtasklibrary.net.response.TaskListRes;
import com.slicejobs.algsdk.algtasklibrary.net.response.ZddResponse;

import java.util.List;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by nlmartian on 7/9/15.
 */
public interface Api {

    @FormUrlEncoded
    @POST("/app/authorize/login")
    public Observable<ZddResponse<LoginRes>> login(
            @Field("appId") String appId,
            @Field("userId") String userId,
            @Field("mobile") String mobile,
            @Field("actionTime") String actionTime,
            @Field("sign") String sign);

    @GET("/vcode")
    public Observable<Response<List>> getVCode(
            @Query("cellphone") String cellphone,
            @Query("timestamp") String timestamp,
            @Query("appId") String appId,
            @Query("sig") String sig);

    @FormUrlEncoded
    @POST("/app/authorize/bind")
    public Observable<ZddResponse<LoginRes>> bind(
            @Field("appId") String appId,
            @Field("userId") String userId,
            @Field("mobile") String mobile,
            @Field("actionTime") String actionTime,
            @Field("sign") String sign,
            @Field("vcode") String vcode);

    @FormUrlEncoded
    @POST("/user_task_manage")
    public Observable<Response<Task>> updateCacheTemplateTaskStatus(//缓存任务结束使用
                                                                    @Query("userid") String userid,
                                                                    @Query("op") String operation,
                                                                    @Query("taskid") String taskid,
                                                                    @Field("templateresultjson") String resultJson,
                                                                    @Field("location") String location,
                                                                    @Field("checkinlocation") String checkinlocation,
                                                                    @Field("cacheuploadstatus") String status,
                                                                    @Query("timestamp") String timestamp,
                                                                    @Query("sec_consumed") String sec_consumed,
                                                                    @Query("interrupted_times") String interrupted_times,
                                                                    @Query("outrange_times") String outrange_times,
                                                                    @Query("appId") String appId,
                                                                    @Field("sig") String sig
    );

    @FormUrlEncoded
    @POST("/sjuser_order_manage")
    public Observable<Response<Task>> newFinishOrder(//缓存任务结束使用
                                                     @Query("userid") String userid,
                                                     @Query("op") String operation,
                                                     @Query("orderid") String orderid,
                                                     @Field("templateresultjson") String resultJson,
                                                     @Field("location") String location,
                                                     @Field("checkinlocation") String checkinlocation,
                                                     @Field("cacheuploadstatus") String status,
                                                     @Query("timestamp") String timestamp,
                                                     @Query("sec_consumed") String sec_consumed,
                                                     @Query("interrupted_times") String interrupted_times,
                                                     @Query("outrange_times") String outrange_times,
                                                     @Field("market_gatherinfo") String marketGatherinfo,
                                                     @Query("appId") String appId,
                                                     @Field("sig") String sig
    );
    @FormUrlEncoded
    @POST("/sjuser_order_manage")
    public Observable<Response<Task>> newFinishOrder(//缓存任务结束使用
                                                     @Query("userid") String userid,
                                                     @Query("op") String operation,
                                                     @Query("orderid") String orderid,
                                                     @Field("templateresultjson") String resultJson,
                                                     @Field("location") String location,
                                                     @Field("checkinlocation") String checkinlocation,
                                                     @Field("cacheuploadstatus") String status,
                                                     @Query("timestamp") String timestamp,
                                                     @Query("sec_consumed") String sec_consumed,
                                                     @Query("interrupted_times") String interrupted_times,
                                                     @Query("outrange_times") String outrange_times,
                                                     @Query("appId") String appId,
                                                     @Field("sig") String sig
    );

    @GET("/upload_ticket")
    public Response<OSSTicket> getOSSTicket(
            @Query("userid") String userid,
            @Query("ossid") String ossid,
            @Query("timestamp") String timestamp,
            @Query("appId") String appId,
            @Query("sig") String sig
    );


    @GET("/user_task?pagesize=20")
    public Observable<Response<TaskListRes>> getMyTasks(
            @Query("userid") String userId,
            @Query("start") int start,
            @Query("status") String status,
            @Query("timestamp") String timestamp,
            @Query("appId") String appId,
            @Query("sig") String sig
    );

    @GET("/user_task?pagesize=20")
    public Observable<Response<TaskListRes>> getMyTodayTasks(
            @Query("userid") String userId,
            @Query("date") String date,
            @Query("start") int start,
            @Query("timestamp") String timestamp,
            @Query("appId") String appId,
            @Query("sig") String sig
    );

    @GET("/index.php?action=task_nearby&pagesize=100&cellphonetype=10")
    public Observable<Response<TaskListRes>> getMyNearbyTasks(
            @Query("userid") String userId,
            @Query("start") int start,
            @Query("orderby") String orderBy,
            @Query("distance") String distance,
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("timestamp") String timestamp,
            @Query("appId") String appId,
            @Query("sig") String sig
    );

    @GET("/index.php?action=task_nearby&pagesize=20&cellphonetype=10")
    public Observable<Response<TaskListRes>> getMyNearbyTwentyTasks(
            @Query("userid") String userId,
            @Query("start") int start,
            @Query("orderby") String orderBy,
            @Query("distance") String distance,
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("timestamp") String timestamp,
            @Query("appId") String appId,
            @Query("sig") String sig
    );

    @GET("/index.php?action=task_nearby")
    public Observable<Response<TaskListRes>> getMyNearbyTasks(
            @Query("userid") String userId,
            @Query("distance") String distance,
            @Query("pagesize") String pagesize,
            @Query("cellphonetype") String cellphonetype,
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("lon_user") String lon_user,
            @Query("lat_user") String lat_user,
            @Query("timestamp") String timestamp,
            @Query("appId") String appId,
            @Query("sig") String sig
    );

    @GET("/index.php?action=task_nearby")
    public Observable<Response<TaskListRes>> getMyNearbyTasksByKeyword(
            @Query("userid") String userId,
            @Query("distance") String distance,
            @Query("pagesize") String pagesize,
            @Query("cellphonetype") String cellphonetype,
            @Query("start") String start,
            @Query("orderby") String orderby,
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("keyword") String keyword,
            @Query("timestamp") String timestamp,
            @Query("appId") String appId,
            @Query("sig") String sig
    );

    @FormUrlEncoded
    @POST("/add_market_map_comments")
    public Observable<Response<Object>> commitMapComments(
            @Query("userid") String userid,
            @Query("marketid") String marketid,
            @Query("score") String score,
            @Query("comment") String comment,
            @Query("appId") String appId,
            @Field("sig") String sig
    );

}
