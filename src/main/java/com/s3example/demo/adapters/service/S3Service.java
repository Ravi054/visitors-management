package com.s3example.demo.adapters.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s3example.demo.adapters.model.VisitorsModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3Client;

    //Bucket level operations

    public void createS3Bucket(String bucketName, boolean publicBucket) {
        if(amazonS3Client.doesBucketExist(bucketName)) {
            log.info("Bucket name already in use. Try another name.");
            return;
        }
        if(publicBucket) {
            amazonS3Client.createBucket(bucketName);
        } else {
            amazonS3Client.createBucket(new CreateBucketRequest(bucketName).withCannedAcl(CannedAccessControlList.Private));
        }
    }

    public List<Bucket> listBuckets(){
        return amazonS3Client.listBuckets();
    }

    public void deleteBucket(String bucketName){
        try {
            amazonS3Client.deleteBucket(bucketName);
        } catch (AmazonServiceException e) {
            log.error(e.getErrorMessage());
            return;
        }
    }

    //Object level operations
    public void putObject(String bucketName, VisitorsModel visitorsModel) throws IOException {

        if(StringUtils.isNotBlank(visitorsModel.getFileName())) {
            amazonS3Client.deleteObject(bucketName, visitorsModel.getFileName());
        }

        JSONObject object = new JSONObject(visitorsModel);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
        String strDate = formatter.format(date);
        System.out.println("Date Format with yyyy-MM-dd : "+strDate);
        String objectName = strDate + ".json";
        File file = new File("." + File.separator + objectName);
        FileWriter fileWriter = new FileWriter(file, false);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println(object);
        printWriter.flush();
        printWriter.close();
        try {
            if(true) {
                var putObjectRequest = new PutObjectRequest(bucketName, objectName, file).withCannedAcl(CannedAccessControlList.PublicRead);
                amazonS3Client.putObject(putObjectRequest);
            } else {
                var putObjectRequest = new PutObjectRequest(bucketName, objectName, file).withCannedAcl(CannedAccessControlList.Private);
                amazonS3Client.putObject(putObjectRequest);
            }
        } catch (Exception e){
            log.error("Some error has ocurred.");
        }
    }


    public Map<String, VisitorsModel> listObjects(String bucketName, String fromDate, String toDate){

        LocalDate localDateFromDate = LocalDate.parse(fromDate);
        LocalDate localDateToDate = LocalDate.parse(toDate).plusDays(1);

        List<LocalDate> listOfDates = localDateFromDate.datesUntil(localDateToDate).collect(Collectors.toList());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        List<String> formattedDates = listOfDates.stream().map(date -> date.format(formatter)).collect(Collectors.toList());

        ObjectListing objectListing = amazonS3Client.listObjects(bucketName);
        Map<String, VisitorsModel> modelMap = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        objectListing.getObjectSummaries().stream()
                .forEach(k ->  {
                    try (final S3Object s3Object = amazonS3Client.getObject(k.getBucketName(),
                            k.getKey());
                         final InputStreamReader streamReader = new InputStreamReader(s3Object.getObjectContent(), StandardCharsets.UTF_8);
                         final BufferedReader reader = new BufferedReader(streamReader)) {
                        if(formattedDates.stream().anyMatch(str -> k.getKey().contains(str))) {
                            JSONTokener tokener = new JSONTokener(reader);
                            JSONObject object = new JSONObject(tokener);
                            VisitorsModel model = mapper.readValue(object.toString(), VisitorsModel.class);

                            modelMap.put(k.getKey(), model);
                        }

                    } catch (final IOException e) {
                        log.error(e.getMessage(), e);
                    }
                });
        return modelMap;
    }

    public void downloadObject(String bucketName, String objectName){
        S3Object s3object = amazonS3Client.getObject(bucketName, objectName);
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        try {
            FileUtils.copyInputStreamToFile(inputStream, new File("." + File.separator + objectName));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void deleteObject(String bucketName, String objectName){
        amazonS3Client.deleteObject(bucketName, objectName);
    }

    public void deleteMultipleObjects(String bucketName, List<String> objects){
        DeleteObjectsRequest delObjectsRequests = new DeleteObjectsRequest(bucketName)
                .withKeys(objects.toArray(new String[0]));
        amazonS3Client.deleteObjects(delObjectsRequests);
    }

    public void moveObject(String bucketSourceName, String objectName, String bucketTargetName){
        amazonS3Client.copyObject(
                bucketSourceName,
                objectName,
                bucketTargetName,
                objectName
        );
    }

}
