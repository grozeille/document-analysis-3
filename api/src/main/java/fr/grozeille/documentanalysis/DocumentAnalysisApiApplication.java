package fr.grozeille.documentanalysis;

import lombok.var;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@SpringBootApplication
@Controller
public class DocumentAnalysisApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentAnalysisApiApplication.class, args);
	}
}
