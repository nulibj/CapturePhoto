package com.client.capturephoto.adapter;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageInfo implements Parcelable
{
    
    public String fullName;
    
    public String imageUri;
    
    public String imageCode;
    
    public ImageInfo()
    {
    };
    
    public ImageInfo(Parcel source)
    {
        
        fullName = source.readString();
        imageUri = source.readString();
        imageCode = source.readString();
        // bitmap = source.readParcelable(null); // 这个地方可以为null
        
    }
    
    @Override
    public int describeContents()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        // TODO Auto-generated method stub
        dest.writeString(fullName);
        dest.writeString(imageUri);
        dest.writeString(imageCode);
        // dest.writeParcelable(bitmap, flags);
        
    }
    
    public static final Parcelable.Creator<ImageInfo> CREATOR = new Parcelable.Creator<ImageInfo>()
    {
        // 重写Creator
        
        @Override
        public ImageInfo createFromParcel(Parcel source)
        {
            return new ImageInfo(source);
        }
        
        @Override
        public ImageInfo[] newArray(int size)
        {
            // TODO Auto-generated method stub
            return new ImageInfo[size];
        }
    };
    
}
