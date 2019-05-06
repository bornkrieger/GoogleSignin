package com.example.googlesignin;

public class Contact {
    int _id;
    String _name;
    String _user_image;
    byte[] image;
    public Contact(){   }

    public Contact(int _id, String _name, String _user_image, byte[] image) {
        this._id = _id;
        this._name = _name;
        this._user_image = _user_image;
        this.image = image;
    }

    public Contact(String name, String _phone_number){
        this._name = name;
        this._user_image = _phone_number;
    }

    public Contact(int _id, String _name, byte[] image) {
        this._id = _id;
        this._name = _name;
        this.image = image;
    }

    public int getID(){
        return this._id;
    }

    public void setID(int id){
        this._id = id;
    }

    public String getName(){
        return this._name;
    }

    public void setName(String name){
        this._name = name;
    }

    public String getPhoneNumber(){
        return this._user_image;
    }

    public void setPhoneNumber(String phone_number){
        this._user_image = phone_number;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
