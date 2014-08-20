package org.magnum.dataup.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.HashedMap;
import org.magnum.dataup.VideoFileManager;
import org.magnum.dataup.VideoSvcApi;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * 
 * @author chainer
 *
 */

//Tell Spring that this class is a Controller that should 
//handle certain HTTP requests for the DispatcherServlet
@Controller
public class VideoController {
	public static final String VIDEO_DATA_FORMAT = "video/mp4";
	private String host = getUrlBaseForLocalServer();
	// An in-memory list that the servlet uses to store the
	// videos that are sent to it by clients
	private Map<Long, Video> videos = new HashMap<>();
	private VideoFileManager videoDataMgr;
	
	// Receives GET requests to /video and returns the current
	// list of videos in memory. Spring automatically converts
	// the list of videos to JSON because of the @ResponseBody
	// annotation.
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return videos.values();
	}

	// Receives POST requests to /video and converts the HTTP
	// request body, which should contain json, into a Video
	// object before adding it to the list. The @RequestBody
	// annotation on the Video parameter is what tells Spring
	// to interpret the HTTP request body as JSON and convert
	// it into a Video object to pass into the method. The
	// @ResponseBody annotation tells Spring to conver the
	// return value from the method back into JSON and put
	// it into the body of the HTTP response to the client.
	//
	// The VIDEO_SVC_PATH is set to "/video" in the VideoSvcApi
	// interface. We use this constant to ensure that the 
	// client and service paths for the VideoSvc are always
	// in synch.
	//
	// For some ways to improve the validation of the data
	// in the Video object, please see this Spring guide:
	// http://docs.spring.io/spring/docs/3.2.x/spring-framework-reference/html/validation.html#validation-beanvalidation
	//
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		// * The video metadata is provided as an application/json request body. The JSON should generate a 
		//   valid instance of the Video class when deserialized by Spring's default Jackson library.
		
		// * The server should generate a unique identifier for the Video object and assign it to the Video 
		//   by calling its setId(...) method.
		// * No video should have ID = 0. All IDs should be > 0
		v.setId( videos.size() + 1 );	//	generate unique video id
		
		// * The server should also generate a "data url" for the Video. The "data url" is the url of the 
		//   binary data for a Video (e.g., the raw mpeg data). The URL should be the full URL for the video 
		//   and not just the path (e.g., http://localhost:8080/video/1/data would be a valid data url). 
		// * See the Hints section for some ideas on how to generate this URL.
		
		v.setDataUrl(host + VideoSvcApi.VIDEO_DATA_PATH.replace("{id}", v.getId() + ""));
		
		videos.put(v.getId(), v);
		
		// * Returns the JSON representation of the Video object that was stored along with any updates to 
		//   that object made by the server.
		return v;
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody
	VideoStatus setVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id, 
			@RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData, HttpServletResponse response) throws IOException {
		
		if (videoDataMgr == null) {
            videoDataMgr = VideoFileManager.get();
        }
		
		if(videos.containsKey(id)) {
			Video video = videos.get(id);
			videoDataMgr.saveVideoData(video, videoData.getInputStream());
			return new VideoStatus(VideoState.READY);
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			throw new ResourceNotFoundException();
		}
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET, produces=VIDEO_DATA_FORMAT)
	public void getData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id, HttpServletResponse response) throws IOException {
		if(videos.containsKey(id)) {
			Video video = videos.get(id);
			if(videoDataMgr.hasVideoData(video)) {
				videoDataMgr.copyVideoData(video, response.getOutputStream());
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	
	private String getUrlBaseForLocalServer() {
        return "http://localhost:8080";
    }
}
