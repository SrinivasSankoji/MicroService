package com.chary.bhaumik.moviecatalog.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Movie implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String movieId;
	private String  name;

}
