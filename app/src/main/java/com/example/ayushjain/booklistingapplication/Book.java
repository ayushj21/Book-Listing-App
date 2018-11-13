package com.example.ayushjain.booklistingapplication;

public class Book {

    String mauthor;
    String mtitle;

    public Book (String author, String title) {
        mauthor = author;
        mtitle = title;
    }

    public String getAuthor()
    {
        return mauthor;
    }

    public String getTitle()
    {
        return mtitle;
    }
}
