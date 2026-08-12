package com.example.CivicTrack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint extends BaseEntity {

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326); // 4326 = standard GPS coordinate system

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(columnDefinition = "geometry(Point,4326)")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Point location;

    private String area;
    private String road;
    private String pincode;

    @Enumerated(EnumType.STRING)
    private Status status;

    private int upvotes;
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnoreProperties({"complaints"})
    private Department department;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private LocalDateTime dueDate;

    private boolean escalated;
    private LocalDateTime escalatedAt;

    // Keeps every existing usage of getLatitude()/getLongitude() working unchanged
    public void setLatLng(double lat, double lng) {
        this.location = GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
    }

    public Double getLatitude() {
        return location != null ? location.getY() : null;
    }

    public Double getLongitude() {
        return location != null ? location.getX() : null;
    }
}