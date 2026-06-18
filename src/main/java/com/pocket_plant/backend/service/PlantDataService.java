package com.pocket_plant.backend.service;

import com.pocket_plant.backend.entity.PlantData;
import com.pocket_plant.backend.repository.PlantDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class PlantDataService {

    private final PlantDataRepository plantDataRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public void fetchAndSavePlants(String apiKey) throws Exception{
        String listUrl = "http://api.nongsaro.go.kr/service/garden/gardenList?apiKey=" + apiKey + "&numOfRows=1000";
        String listXml = restTemplate.getForObject(listUrl, String.class);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document listDoc = builder.parse(new ByteArrayInputStream(listXml.getBytes("UTF-8")));
        NodeList items = listDoc.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String cntntsNo = getTagValue("cntntsNo", item);
            String cntntsSj = getTagValue("cntntsSj", item);

            if (!cntntsNo.isEmpty()) {
                // 2. 알아낸 컨텐츠 번호로 상세 정보 조회 URL 호출
                String detailUrl = "http://api.nongsaro.go.kr/service/garden/gardenDtl?apiKey=" + apiKey + "&cntntsNo=" + cntntsNo;
                String detailXml = restTemplate.getForObject(detailUrl, String.class);

                Document detailDoc = builder.parse(new ByteArrayInputStream(detailXml.getBytes("UTF-8")));
                Element detailItem = (Element) detailDoc.getElementsByTagName("item").item(0);

                if (detailItem != null) {
                    // 3. 데이터 매핑 및 Entity 생성
                    PlantData plant = new PlantData();
                    plant.setCntntNo(Long.parseLong(cntntsNo));
                    plant.setPlantName(cntntsSj);
                    plant.setGrowhTp(getTagValue("grwhTpCodeNm", detailItem));
                    plant.setWinterTemperature(getTagValue("winterLwetTpCodeNm", detailItem));
                    plant.setHumidity(getTagValue("hdCodeNm", detailItem));
                    plant.setWaterCycleSpring(getTagValue("watercycleSprngCodeNm", detailItem));

                    // 4. DB에 최종 저장
                    plantDataRepository.save(plant);
                    System.out.println("식물 저장 완료: " + plant.getPlantName());
                }
            }
        }
    }

    // XML 태그 안의 텍스트 값을 안전하게 가져오는 메서드
    private String getTagValue(String tag, Element element) {
        NodeList nlList = element.getElementsByTagName(tag);
        if (nlList != null && nlList.getLength() > 0) {
            if (nlList.item(0).getChildNodes().getLength() > 0) {
                return nlList.item(0).getChildNodes().item(0).getNodeValue();
            }
        }
        return "";
    }

    public java.util.List<PlantData> searchPlants(String keyword) {
        return plantDataRepository.findByPlantNameContaining(keyword);
    }
}