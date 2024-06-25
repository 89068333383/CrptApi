package org.example;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.IOException;
import java.util.List;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi{

    private TimeUnit timeUnit;
    private int requestLimit;
    private int requestCount;
    private boolean limit;

    private Semaphore semaphore;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        if (requestLimit > 0) {
            this.requestLimit = requestLimit;
            this.requestCount = requestLimit;
            semaphore = new Semaphore(requestLimit);
        } else {
            this.requestLimit = 1;
            this.requestCount = 1;
        }
    }

    public void setRequestLimit(int requestLimit) {
        this.requestLimit = requestLimit;
    }

    public boolean limitShek(int idenThread){

        if (requestCount>0) {
            limit = true;
            requestCount--;
        }else {
            try {
//                Thread.sleep(1000, timeUnit.ordinal());
//                TimeUnit.values(timeUnit).;
                timeUnit.sleep(10);
                System.out.println(timeUnit);
                System.out.println(" процесс  приостановлен - " + idenThread + " = " + requestCount);
                requestCount = requestLimit;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            limit = true;
        }
        return limit;
    }

    public void runClass(){
        for (int i = 1; i < 21; i++) {
            System.out.println("Запуск потока - " + i);
            if (limitShek(i)) {
                new ApiRun(semaphore, i).start();
            }
        }
    }




}
//____thread____

class ApiRun extends Thread {
    private Semaphore semaphore;
    private int identThread;
    private TimeUnit timeUnit;
    private int requestLimit;
    private int requestCount;
    private boolean limit;

    public ApiRun(Semaphore semaphore, int identThread) {
        this.semaphore = semaphore;
        this.identThread = identThread;
    }

    public void run() {

        try {
            semaphore.acquire();
            System.out.println(" процесс отправки запущен - " + identThread + " = " + requestCount);
            runRequest();
            System.out.println(" процесс  завершен - " + identThread + " = " + requestCount);
//            limitShek();
            semaphore.release();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


//----отправка сообщения----
    public void runRequest(){
        {
            String str = fileToString();

            System.out.println("---" + str);

            String serviceUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Documents.Document document = gson.fromJson(str, Documents.Document.class);

//        System.out.println(document);

            final Content postResult;
            try {
                postResult = Request.Post("https://ismp.crpt.ru/api/v3/lk/documents/create")
                        .bodyString(str, ContentType.APPLICATION_JSON)
                        .execute().returnContent();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(((Content) postResult).asString());
        }


    }
    public String fileToString(){
        String result;
        try {
            result = Files.toString(new File("/home/alexey/Документы/test/untitled/src/main/resources/JSON.json"), Charsets.UTF_8);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}

    //____models____
    class Documents{

        public class Description {
            private String participantInn;
        }

        public class Product {
            private String certificate_document;
            private String certificate_document_date;
            private String certificate_document_number;
            private String owner_inn;
            private String producer_inn;
            private String production_date;
            private String tnved_code;
            private String uit_code;
            private String uitu_code;
        }
        public class Document {
            private Description description;
            private String doc_id;
            private String doc_status;
            private String doc_type;
            private boolean importRequest;
            private String owner_inn;
            private String participant_inn;
            private String producer_inn;
            private String production_date;
            private String production_type;
            private List<Product> products;
            private String reg_date;
            private String reg_number;
        }

        public Document jsonToDocument(){
            return null;

        }

    }

