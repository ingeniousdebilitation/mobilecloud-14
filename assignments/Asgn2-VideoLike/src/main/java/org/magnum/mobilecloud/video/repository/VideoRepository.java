package org.magnum.mobilecloud.video.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author chainer
 * http://docs.spring.io/spring-data/data-commons/docs/current/reference/html/repositories.html
 * http://docs.spring.io/docs/Spring-MVC-step-by-step/part5.html
 */

@Repository
public interface VideoRepository extends CrudRepository<Video, Long>{
	public Collection<Video> findByName(String title);
	
	public Collection<Video> findByDurationLessThan(long duration);
	
	public Collection<Video> findAll();
}
