package com.example.uploadingfiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.example.uploadingfiles.storage.StorageProperties;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.apache.commons.io.IOUtils;

import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
public class FileUploadController {

	private final StorageService storageService;
	@Autowired
	private ServletContext servletContext;


	//@Autowired
	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {

		List<String> list=storageService.loadAll().map(
				path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList());
		log.info("all Path "+list);
		model.addAttribute("files", storageService.loadAll().map(
				path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList()));

		return "uploadForm";
	}

	@ResponseBody
	@RequestMapping(value = "/file/{filename}", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)

	public void getImage(HttpServletResponse response, @PathVariable String filename) throws IOException {

		ClassPathResource imgFile = new ClassPathResource("image/Capture.PNG");
		Resource resource=storageService.loadAsResource(filename);

		response.setContentType(MediaType.IMAGE_PNG_VALUE);
		StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
	}

//	@ResponseBody
//	@RequestMapping(value = "/file", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
//
//	public void getImage(HttpServletResponse response) throws IOException {
//
//		ClassPathResource imgFile = new ClassPathResource("image/Capture.PNG");
//		Resource resource=storageService.loadAsResource("Capture.PNG");
//
//		response.setContentType(MediaType.IMAGE_PNG_VALUE);
//		StreamUtils.copy(resource.getInputStream(), response.getOutputStream());
//	}

//	@ResponseBody
//	@GetMapping(value = "/file", produces = MediaType.IMAGE_PNG_VALUE)
//	public byte[] testphoto() throws IOException {
//		InputStream in = servletContext.getResourceAsStream("/resources/image/Capture.PNG");
//		return IOUtils.toByteArray(in);
//	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		HttpHeaders headers =new HttpHeaders();
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,@RequestParam String x, @RequestParam String y,
			RedirectAttributes redirectAttributes,HttpServletRequest request) {

		int horizontalSeam=Integer.parseInt(x);
		int verticalSeam=Integer.parseInt(y);

		log.info("url"+request.getServletContext().getRealPath("/upload-dir"));
		log.info("url"+request.getScheme() + "://" + request.getServerName() + ":" +request.getServerPort() + "/upload-dir/");
		String[] filename=storageService.store(file,horizontalSeam,verticalSeam);
		log.info(filename[0]);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");
		redirectAttributes.addFlashAttribute("origin","/file/"+filename[0]);
		redirectAttributes.addFlashAttribute("edited","/file/"+filename[1]);

		return "redirect:/";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
