package com.example.greyson.test1.net;

import com.example.greyson.test1.entity.SafePlaceRes;

import java.util.Map;

import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by greyson on 1/4/17.
 */

public interface WSNetService {
    /**
     * SafePlace Request
     */
    @POST("?format=api&suburb=Malvern+East")
    Observable<SafePlaceRes> getSafePlaceData(@QueryMap Map<String, String> params);
}
