package com.example.appointmentscheduler.entity;

import com.example.appointmentscheduler.entity.user.customer.Customer;
import com.example.appointmentscheduler.entity.user.provider.Provider;
import com.example.appointmentscheduler.entity.user.User;
import com.example.appointmentscheduler.model.AppointmentSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@Entity
@Table(name = "appointments")
@JsonSerialize(using = AppointmentSerializer.class)
public class Appointment extends BaseEntity implements Comparable<Appointment> {

    @Column(name = "start")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime start;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "end")
    private LocalDateTime end;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @OneToOne
    @JoinColumn(name = "id_canceler")
    private User canceler;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AppointmentStatus status;

    @ManyToOne
    @JoinColumn(name = "id_customer")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "id_provider")
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "id_work")
    private Work work;

    @OneToMany(mappedBy = "appointment")
    private List<ChatMessage> chatMessages;

    @ManyToOne
    @JoinColumn(name = "id_invoice")
    private Invoice invoice;
//
//    @OneToOne(mappedBy = "requested", cascade = {CascadeType.ALL})
//    private ExchangeRequest exchangeRequest;
    @Column(name = "barcode_id")
    private Long barcodeId;

    @Column(name = "barcode_image")
    private String barcodeImage;

    public Appointment() {
    }

    public Appointment(LocalDateTime start, LocalDateTime end, Customer customer, Provider provider, Work work) {
        this.start = start;
        this.end = end;
        this.customer = customer;
        this.provider = provider;
        this.work = work;
    }

    @Override
    public int compareTo(Appointment o) {
        return this.getStart().compareTo(o.getStart());
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Work getWork() {
        return work;
    }

    public void setWork(Work work) {
        this.work = work;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public List<ChatMessage> getChatMessages() {
        Collections.sort(chatMessages);
        return chatMessages;
    }

    public User getCanceler() {
        return canceler;
    }

    public void setCanceler(User canceler) {
        this.canceler = canceler;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
////DELETE:    public ExchangeRequest getExchangeRequest() {
//    public ExchangeRequest getExchangeRequest() {
//        return exchangeRequest;
//    }

//    public void setExchangeRequest(ExchangeRequest exchangeRequest) {
//        this.exchangeRequest = exchangeRequest;
//    }

    public Long getBarcodeId() {
        return barcodeId;
    }

    public void setBarcodeId(Long barcodeId) {
        this.barcodeId = barcodeId;
    }

    public String getBarcodeImage() {
        return barcodeImage;
    }

    public void setBarcodeImage(String barcodeImage) {
        this.barcodeImage = barcodeImage;
    }

}
