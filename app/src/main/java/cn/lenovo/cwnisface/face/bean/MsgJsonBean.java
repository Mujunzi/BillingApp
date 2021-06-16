package cn.lenovo.cwnisface.face.bean;

/**
 * Created by baohm1 on 2018/3/13.
 */

public class MsgJsonBean {
    /**
     * status : 0
     * errMsg :
     * data : {"user":{"similar":0.9849412,"uID":"93","gender":2,"trackID":2,"faceID":445}}
     */
    private int status;
    private String errMsg;
    private DataBean data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * user : {"similar":0.9849412,"uID":"93","gender":2,"trackID":2,"faceID":445}
         */

        private UserBean user;

        public UserBean getUser() {
            return user;
        }

        public void setUser(UserBean user) {
            this.user = user;
        }

        public static class UserBean {
            /**
             * similar : 0.9849412
             * uID : 93
             * gender : 2
             * trackID : 2
             * faceID : 445
             */

            private double similar;
            private String uID;
            private int gender;
            private int trackID;
            private int faceID;

            public double getSimilar() {
                return similar;
            }

            public void setSimilar(double similar) {
                this.similar = similar;
            }

            public String getUID() {
                return uID;
            }

            public void setUID(String uID) {
                this.uID = uID;
            }

            public int getGender() {
                return gender;
            }

            public void setGender(int gender) {
                this.gender = gender;
            }

            public int getTrackID() {
                return trackID;
            }

            public void setTrackID(int trackID) {
                this.trackID = trackID;
            }

            public int getFaceID() {
                return faceID;
            }

            public void setFaceID(int faceID) {
                this.faceID = faceID;
            }
        }
    }
}
