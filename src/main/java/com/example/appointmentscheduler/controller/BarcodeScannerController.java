package com.example.appointmentscheduler.controller;

import com.example.appointmentscheduler.service.BarcodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class BarcodeScannerController {

    @Autowired
    private BarcodeService barcodeService;

    @GetMapping("/scan/{barcode}")
    @ResponseBody
    public String scanBarcode(@PathVariable String barcode) {
        return barcodeService.scanBarcode(barcode);
    }

}
