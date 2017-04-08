package com.example.greyson.test1.entity;

import java.util.List;

/**
 * Created by greyson on 1/4/17.
 */

public class SafePlaceRes {


    /**
     * status : 200
     * message : 2 KM
     * results : [{"id":16,"establishment":"McDonald's","address":"484 Malvern Rd","suburb":"Prahran","postcode":3181,"state":"VIC","type":"Restaurant","latitude":"-37.848182","longitude":"145.002577"},{"id":22,"establishment":"McDonald's","address":"411-423 Bell Street (Cnr St Georges Road)","suburb":"Preston ","postcode":3072,"state":"VIC","type":"Restaurant","latitude":"-37.744537","longitude":"144.997496"},{"id":86,"establishment":"McDonald's","address":"100 Waverley Rd","suburb":"Malvern East","postcode":3145,"state":"VIC","type":"Restaurant","latitude":"-37.876095","longitude":"145.047812"},{"id":90,"establishment":"McDonald's","address":"692 Glenferrie Rd","suburb":"Hawthorn ","postcode":3122,"state":"VIC","type":"Restaurant","latitude":"-37.820573","longitude":"145.035927"},{"id":145,"establishment":"Hungry Jack's","address":"62 Cook Street","suburb":"PORT MELBOURNE","postcode":3207,"state":"VIC","type":"Restaurant","latitude":"-37.830742","longitude":"144.915618"}]
     */

    private int status;
    private String message;
    private List<ResultsBean> results;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ResultsBean> getResults() {
        return results;
    }

    public void setResults(List<ResultsBean> results) {
        this.results = results;
    }

    public static class ResultsBean {
        /**
         * id : 16
         * establishment : McDonald's
         * address : 484 Malvern Rd
         * suburb : Prahran
         * postcode : 3181
         * state : VIC
         * type : Restaurant
         * latitude : -37.848182
         * longitude : 145.002577
         */

        private String establishment;
        private String type;
        private Double latitude;
        private Double longitude;

        public String getEstablishment() {
            return establishment;
        }

        public void setEstablishment(String establishment) {
            this.establishment = establishment;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }
}
