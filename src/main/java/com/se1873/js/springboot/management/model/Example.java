package com.se1873.js.springboot.management.model;

import jakarta.persistence.*;

@Entity
@Table(name = "test")
public class Example {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "NAME")
  private String name;

  public Example() {}

  public Example(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
