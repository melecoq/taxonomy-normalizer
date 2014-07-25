package org.gbif.taxonomy.norm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class NormClassification<T> {

  private Integer id;
  private Integer parentId;
  private String scientificName;
  private String author;
  private String rank;
  private final List<T> payloads = new ArrayList<T>();

  public NormClassification() {
    // default constructor which doesn't initialize any fields
  }

  public NormClassification(Integer id, String scientificName, String author, String rank) {
    this.id = id;
    this.scientificName = scientificName;
    this.author = author;
    this.rank = rank;
  }

  public NormClassification(Integer id, Integer parentId, String scientificName, String author, String rank) {
    this.id = id;
    this.parentId = parentId;
    this.scientificName = scientificName;
    this.author = author;
    this.rank = rank;
  }

  @Override
  public String toString() {
    return "id[" + id + "], parentId[" + parentId + "], scientificName[" + scientificName + "], author[" + author +
      "], rank[" + rank + "]";
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getParentId() {
    return parentId;
  }

  public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }

  public String getScientificName() {
    return scientificName;
  }

  public void setScientificName(String scientificName) {
    this.scientificName = scientificName;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getRank() {
    return rank;
  }

  public void setRank(String rank) {
    this.rank = rank;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, true);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  public List<T> getPayloads() {
    return payloads;
  }
}

