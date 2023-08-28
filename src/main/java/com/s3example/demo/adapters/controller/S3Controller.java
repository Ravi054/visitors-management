package com.s3example.demo.adapters.controller;

import com.amazonaws.services.s3.model.Bucket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s3example.demo.adapters.model.VisitorsModel;
import com.s3example.demo.adapters.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/buckets/")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping(value = "/{bucketName}")
    public void createBucket(@PathVariable String bucketName, @RequestParam boolean publicBucket){
        s3Service.createS3Bucket(bucketName, publicBucket);
    }

    @GetMapping
    public List<String> listBuckets(){
        var buckets = s3Service.listBuckets();
        var names = buckets.stream().map(Bucket::getName).collect(Collectors.toList());
        return names;
    }

    @DeleteMapping(value = "/{bucketName}")
    public void deleteBucket(@PathVariable String bucketName){
        s3Service.deleteBucket(bucketName);
    }

    @PostMapping(value = "/objects", consumes = "application/json")
    public String createObject(@RequestBody VisitorsModel visitorsModel) throws IOException, ParseException {
        if (visitorsModel.getName() != null && visitorsModel.getName().trim() != "") {
            s3Service.putObject("test-ravi-bucket2", visitorsModel);
            return "Ok";
        }
        return "Invalid Data";
    }

    @GetMapping(value = "/downloadObjects")
    public ResponseEntity<Resource> downloadObject(@RequestParam String fromDate, @RequestParam String toDate) throws IOException {
        Map<String, VisitorsModel> listMap = s3Service.listObjects("test-ravi-bucket2", fromDate, toDate);

        String newFileName = "VisitorModelData";

        Path path = convertJsonToCsv(listMap.values().stream().collect(Collectors.toList()), "./" + newFileName);
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + newFileName);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }

    public Path convertJsonToCsv(List<VisitorsModel> visitorsModelList, String newFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CSVWriter writer = new CSVWriter(new FileWriter(newFilePath));
        writer.writeNext(new String[]{"Name", "Number", "Whom To Meet", "Purpose", "Parcel", "In Time", "Out Time", "Remark"});
        Map<String, Object> map = new HashMap<>();
        visitorsModelList.forEach(visitorModel -> {
        String parcel = "No";
        if (visitorModel.getParcel() == 1) {
         parcel = "Yes";
        }
            writer.writeNext(new String[] {visitorModel.getName(), visitorModel.getNumber(),
                    visitorModel.getWhomToMeet(), visitorModel.getPurpose(), parcel,
            visitorModel.getInTime(), visitorModel.getOutTime(), visitorModel.getRemark()});

        });


      //  writer.writeNext(map.values().toArray(new String[0]));
        writer.close();
        File csvFile = new File(newFilePath);

        return csvFile.toPath();
    }

    @PatchMapping(value = "/{bucketSourceName}/objects/{objectName}/{bucketTargetName}")
    public void moveObject(@PathVariable String bucketSourceName, @PathVariable String objectName, @PathVariable String bucketTargetName) {
        s3Service.moveObject(bucketSourceName, objectName, bucketTargetName);
    }

    @GetMapping(value = "/objects")
    public Map<String, VisitorsModel> listObjects(@RequestParam String fromDate, @RequestParam String toDate) {
        return s3Service.listObjects("test-ravi-bucket2", fromDate, toDate);
    }

    @DeleteMapping(value = "/objects/{objectName}")
    public void deleteObject(@PathVariable String objectName) {
        s3Service.deleteObject("test-ravi-bucket2", objectName);
    }

    @DeleteMapping(value = "/{bucketName}/objects")
    public void deleteObject(@PathVariable String bucketName, @RequestBody List<String> objects) {
        s3Service.deleteMultipleObjects(bucketName, objects);
    }

}
