package com.github.bradjacobs.stock;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

// todo
//   better class Name
//   better class Home
//   location that can serialize/deserialize this.
public class GenericNode
{
    private String id;
    private String name;
    private List<GenericNode> children = new ArrayList<>();

    @JsonIgnore
    private GenericNode parentNode;


    public GenericNode()
    {
    }

    public GenericNode(String id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GenericNode> getChildren() {
        return children;
    }

    public void setChildren(List<GenericNode> children) {
        this.children = children;
    }

    public GenericNode getParentNode()
    {
        return parentNode;
    }

    public void setParentNode(GenericNode parentNode)
    {
        this.parentNode = parentNode;
    }
}
