package org.magnum.mobilecloud.video.controller;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

import com.google.common.collect.Lists;

/**
 * 
 * @author chainer
 *
 */

@Controller
public class VideoSvc {
	// The VideoRepository that we are going to store our videos
	// in. We don't explicitly construct a VideoRepository, but
	// instead mark this object as a dependency that needs to be
	// injected by Spring. Our Application class has a method
	// annotated with @Bean that determines what object will end
	// up being injected into this member variable.
	//
	// Also notice that we don't even need a setter for Spring to
	// do the injection.
	//
	@Autowired
	private VideoRepository videosRepo;

	// Receives GET requests to /video and returns the current
	// list of videos in memory. Spring automatically converts
	// the list of videos to JSON because of the @ResponseBody
	// annotation.
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return Lists.newArrayList(videosRepo.findAll());
	}

	//POST /video
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v, Principal p) {
		v.setLikes(0);
		return videosRepo.save(v);
	}
	
	//GET /video/{id}
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable long id) {
		Video v = videosRepo.findOne(id);
		
		if(v == null) {
			throw new EntityNotFoundException();
		}
		
		return v;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		Collection<Video> videos = videosRepo.findByName(title);
		return videos;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method=RequestMethod.POST)
	public ResponseEntity<Void> likeVideo(@PathVariable("id") long id, Principal p) {
		Video v = videosRepo.findOne(id);
		
		if(v == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);//throw new EntityNotFoundException();
		}
		
		String user = p.getName();
		
		List<String> users = v.getUsers();
		
		if(users.contains(user)) {
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}
		
		users.add(user);
		v.setLikes( users.size() );
		
		videosRepo.save(v);
		
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method=RequestMethod.POST)
	public ResponseEntity<Void> unlikeVideo(@PathVariable("id") long id, Principal p) {
		Video v = videosRepo.findOne(id);
		
		if(v == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		
		String user = p.getName();
		
		List<String> users = v.getUsers();
		
		if(!users.contains(user)) {
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}
		
		users.remove(user);
		
		v.setLikes(users.size());
		
		videosRepo.save(v);
		
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method=RequestMethod.GET)
	public @ResponseBody List<String> getUsersWhoLikedVideo(@PathVariable long id) {
		Video v = videosRepo.findOne(id);
		
		if(v == null) {
			throw new EntityNotFoundException();
		}
		
		return v.getUsers();
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
		Collection<Video> videos = videosRepo.findByDurationLessThan(duration);
		return videos;
	}
}
