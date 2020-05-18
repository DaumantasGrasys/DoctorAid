package com.agobal.DoctorAid.Entities;

import java.io.Serializable;

public class HelpRequest implements Serializable {

    private String Userid;
    private String Description;
    private String DateCreated;
    private Integer Active;


    public HelpRequest (String Userid, String Description, String DateCreated, Integer Active)
    {
        this.Userid = Userid;
        this.Description = Description;
        this.DateCreated = DateCreated;
        this.Active = Active;

    }

    public String getUserid() {
        return Userid;
    }

    public void setUserid(String Userid) {
        Userid = Userid;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        Description = Description;
    }

    public String getDateCreated() { return DateCreated; }

    public void setDateCreated(String DateCreated) { DateCreated = DateCreated; }

    public Integer getActive() { return Active; }

    public void setActive(Integer Active) { Active = Active; }


}
