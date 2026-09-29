package com.cropadvisor;

import jakarta.persistence.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@SpringBootApplication
@EnableScheduling
public class CropAdvisorApplication {
    public static void main(String[] args){ SpringApplication.run(CropAdvisorApplication.class,args); }

    @Entity public static class Region {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
        @Column(unique=true,nullable=false) public String name;
        public Region(){}
    }

    @Entity public static class Farmer {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
        @Column(nullable=false) public String name;
        public String phone,email,crop;
        @ManyToOne public Region region;
        public Farmer(){}
    }

    @Entity public static class Officer {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
        @Column(nullable=false) public String name;
        public String phone,email;
        @ManyToOne public Region region;
        public Officer(){}
    }

    @Entity public static class Ticket {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
        public String cropName,symptoms,photoReference,recommendation,status,priority;
        public String subject,description,advice;
        public LocalDateTime createdAt,updatedAt,closedAt,escalatedAt;
        @ManyToOne public Farmer farmer;
        @ManyToOne public Officer officer;
        @PrePersist void onCreate(){createdAt=updatedAt=LocalDateTime.now();if(status==null)status="PENDING";if(priority==null)priority="NORMAL";}
        @PreUpdate void onUpdate(){updatedAt=LocalDateTime.now();}
    }

    public interface Regions extends JpaRepository<Region,Long>{}
    public interface Farmers extends JpaRepository<Farmer,Long>{}
    public interface Officers extends JpaRepository<Officer,Long>{Optional<Officer> findFirstByRegionId(Long id);}
    public interface Tickets extends JpaRepository<Ticket,Long>{
        List<Ticket> findByFarmerIdOrderByCreatedAtDesc(Long id);
        List<Ticket> findByOfficerIdOrderByCreatedAtDesc(Long id);
    }

    @RestController @RequestMapping("/api") @CrossOrigin(origins="*")
    public static class Api {
        final Regions regions; final Farmers farmers; final Officers officers; final Tickets tickets;
        Api(Regions r,Farmers f,Officers o,Tickets t){regions=r;farmers=f;officers=o;tickets=t;}

        @GetMapping("/regions") public List<Region> regions(){return regions.findAll();}
        @GetMapping("/farmers") public List<Farmer> farmers(){return farmers.findAll();}
        @GetMapping("/officers") public List<Officer> officers(){return officers.findAll();}
        @GetMapping("/tickets") public List<Ticket> tickets(){return tickets.findAll();}
        @GetMapping("/officers/{id}/tickets") public List<Ticket> officerTickets(@PathVariable Long id){
            Officer o=officers.findById(id).orElseThrow(()->new IllegalArgumentException("Officer not found"));
            for(Ticket x:tickets.findAll()){
                if(x.officer==null && x.farmer!=null && x.farmer.region!=null && o.region!=null && Objects.equals(x.farmer.region.id,o.region.id)){
                    x.officer=o; tickets.save(x);
                }
            }
            return tickets.findByOfficerIdOrderByCreatedAtDesc(id);
        }
        @GetMapping("/farmers/{id}/tickets") public List<Ticket> farmerTickets(@PathVariable Long id){return tickets.findByFarmerIdOrderByCreatedAtDesc(id);}

        @PostMapping("/farmers") public Farmer createFarmer(@RequestBody Map<String,Object> body){
            Farmer f=new Farmer(); f.name=String.valueOf(body.getOrDefault("name","")).trim();
            f.phone=String.valueOf(body.getOrDefault("phone",""));
            Object rid=body.get("regionId");
            if(f.name.isBlank()||rid==null) throw new IllegalArgumentException("Name and region are required");
            f.region=regions.findById(Long.valueOf(String.valueOf(rid))).orElseThrow(()->new IllegalArgumentException("Region not found"));
            return farmers.save(f);
        }

        @PostMapping("/tickets") public Ticket createTicket(@RequestBody Map<String,Object> body){
            Long farmerId=Long.valueOf(String.valueOf(body.get("farmerId")));
            Farmer f=farmers.findById(farmerId).orElseThrow(()->new IllegalArgumentException("Farmer not found"));
            Ticket x=new Ticket(); x.farmer=f;
            x.cropName=String.valueOf(body.getOrDefault("cropName",body.getOrDefault("crop","")));
            x.symptoms=String.valueOf(body.getOrDefault("symptoms",body.getOrDefault("description","")));
            x.photoReference=String.valueOf(body.getOrDefault("photoReference",""));
            x.subject=x.cropName; x.description=x.symptoms; x.priority=String.valueOf(body.getOrDefault("priority","NORMAL"));
            x.status="PENDING";
            if(f.region!=null) officers.findFirstByRegionId(f.region.id).ifPresent(v->x.officer=v);
            return tickets.save(x);
        }

        @PostMapping("/tickets/{id}/recommendation") public Ticket recommendation(@PathVariable Long id,@RequestBody Map<String,Object> body){
            Ticket x=tickets.findById(id).orElseThrow(()->new IllegalArgumentException("Ticket not found"));
            x.recommendation=String.valueOf(body.getOrDefault("recommendation","")).trim();
            x.advice=x.recommendation;
            return tickets.save(x);
        }

        @PutMapping("/tickets/{id}/close") public Ticket close(@PathVariable Long id,@RequestBody(required=false) Map<String,Object> body){
            Ticket x=tickets.findById(id).orElseThrow(()->new IllegalArgumentException("Ticket not found"));
            x.status="CLOSED"; x.closedAt=LocalDateTime.now(); return tickets.save(x);
        }

        @PutMapping("/tickets/{id}/reopen") public Ticket reopen(@PathVariable Long id,@RequestBody(required=false) Map<String,Object> body){
            Ticket x=tickets.findById(id).orElseThrow(()->new IllegalArgumentException("Ticket not found"));
            x.status="PENDING"; x.closedAt=null; x.escalatedAt=null; x.createdAt=LocalDateTime.now(); return tickets.save(x);
        }

        @PutMapping("/tickets/{id}/status") public Ticket status(@PathVariable Long id,@RequestParam String value){
            Ticket x=tickets.findById(id).orElseThrow(()->new IllegalArgumentException("Ticket not found"));
            x.status=value.toUpperCase(); if(x.status.equals("CLOSED"))x.closedAt=LocalDateTime.now(); return tickets.save(x);
        }
    }

    @Component
    public static class EscalationJob {
        final Tickets tickets;
        EscalationJob(Tickets t){tickets=t;}
        @Scheduled(fixedDelay=600000)
        public void escalatePending(){
            LocalDateTime cutoff=LocalDateTime.now().minusHours(48);
            for(Ticket x:tickets.findAll()){
                if("PENDING".equalsIgnoreCase(x.status) && x.createdAt!=null && x.createdAt.isBefore(cutoff)){
                    x.status="ESCALATED"; x.escalatedAt=LocalDateTime.now(); tickets.save(x);
                }
            }
        }
    }
}