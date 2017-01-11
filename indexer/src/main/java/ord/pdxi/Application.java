
package ord.pdxi;

import org.pdxi.dao.Patient;
import org.pdxi.dao.Tumor;
import org.pdxi.dao.TumorType;
import org.pdxi.repositories.PatientRepository;
import org.pdxi.repositories.TumorTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SpringBootApplication
@EnableNeo4jRepositories
public class Application {

    private final static Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner demo(PatientRepository patientRepository, TumorTypeRepository tumorTypeRepository) {
        return args -> {

            patientRepository.deleteAll();

            Patient m54 = new Patient("1", "M", "54", null, null);
            Patient m34 = new Patient("2", "M", "34", null, null);
            Patient f67 = new Patient("3", "F", "67", null, null);

            TumorType metastatic = tumorTypeRepository.findByName("Metastatic");
            System.out.println(metastatic);
            TumorType recurrent = tumorTypeRepository.findByName("Recurrent/Relapse");
            System.out.println(recurrent);

            Tumor t = new Tumor("tumor 1", metastatic, null, null, null);
            Tumor t2 = new Tumor("tumor 2", recurrent, null, null, null);

            f67.setTumors(new HashSet<>(Arrays.asList(t, t2)));
            List<Patient> team = Arrays.asList(m54, m34, f67);

            log.info("Before linking up with Neo4j...");

            team.stream().forEach(patient -> log.info("\t" + patient.toString()));

            patientRepository.save(m54);
            patientRepository.save(m34);
            patientRepository.save(f67);

//			m54 = patientRepository.findByName(m54.getName());
//			m54.worksWith(m34);
//			m54.worksWith(f67);
//			patientRepository.save(m54);
//
//			m34 = patientRepository.findByName(m34.getName());
//			m34.worksWith(f67);
            // We already know that m34 works with m54
//			patientRepository.save(m34);


//			log.info("Lookup each person by name...");
//			team.stream().forEach(patient -> log.info(
//					"\t" + patientRepository.findByName(patient.getName()).toString()));
        };
    }

}
