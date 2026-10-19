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
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantDataService {

    private final PlantDataRepository plantDataRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public void fetchAndSavePlants(String apiKey) throws Exception {
        String listUrl = "http://api.nongsaro.go.kr/service/garden/gardenList?apiKey=" + apiKey + "&numOfRows=1000";
        String listXml = restTemplate.getForObject(listUrl, String.class);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document listDoc = builder.parse(new ByteArrayInputStream(listXml.getBytes(StandardCharsets.UTF_8)));
        NodeList items = listDoc.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String cntntsNo = getTagValue("cntntsNo", item);
            String cntntsSj = getTagValue("cntntsSj", item);

            if (!cntntsNo.isEmpty()) {
                // 2. 알아낸 컨텐츠 번호로 상세 정보 조회 URL 호출
                String detailUrl = "http://api.nongsaro.go.kr/service/garden/gardenDtl?apiKey=" + apiKey + "&cntntsNo=" + cntntsNo;
                String detailXml = restTemplate.getForObject(detailUrl, String.class);

                Document detailDoc = builder.parse(new ByteArrayInputStream(detailXml.getBytes(StandardCharsets.UTF_8)));
                Element detailItem = (Element) detailDoc.getElementsByTagName("item").item(0);

                if (detailItem != null) {
                    // 3. 데이터 매핑 및 Entity 생성
                    PlantData plant = new PlantData();
                    plant.setCntntNo(Long.parseLong(cntntsNo));
                    plant.setPlantName(cntntsSj);
                    plant.setGrowhTp(getTagValue("grwhTpCodeNm", detailItem));
                    plant.setWinterTemperature(getTagValue("winterLwetTpCodeNm", detailItem));
                    plant.setHumidity(getTagValue("hdCodeNm", detailItem));

                    // 원본 텍스트 추출
                    String waterSpring = getTagValue("watercycleSprngCodeNm", detailItem);
                    String lightDemand = getTagValue("lighttdtlCodeNm", detailItem);

                    plant.setWaterCycleSpring(waterSpring);
                    plant.setLightDemand(lightDemand);

                    // 수분 수치 변환 매핑 (%)
                    int[] moisture = mapWateringToMoisture(waterSpring);
                    plant.setWaterThreshold(moisture[0]);
                    plant.setTargetMoistureMin(moisture[1]);
                    plant.setTargetMoistureMax(moisture[2]);

                    // 광도 수치 변환 매핑 (Lux)
                    int[] lux = mapLightToLux(lightDemand);
                    plant.setMinLux(lux[0]);
                    plant.setMaxLux(lux[1]);

                    // 4. DB에 최종 저장
                    plantDataRepository.save(plant);
                    System.out.println("식물 저장 완료: " + plant.getPlantName()
                            + " [수분기준: " + moisture[0] + "%, 조도: " + lux[0] + "~" + lux[1] + " Lux]");
                }
            }
        }
    }

    // 물주기 텍스트 -> 수분율 수치(%) 변환: {관수임계치, 적정최소, 적정최대}
    private int[] mapWateringToMoisture(String text) {
        if (text == null || text.isEmpty()) {
            return new int[]{30, 45, 65}; // 기본 관엽 기준
        }

        if (text.contains("항상") || text.contains("축축")) {
            return new int[]{50, 60, 80}; // 습생/수생 식물
        } else if (text.contains("표면") || text.contains("겉흙")) {
            return new int[]{30, 45, 65}; // 일반 관엽식물
        } else if (text.contains("대부분")) {
            return new int[]{20, 35, 50}; // 건조에 강한 식물
        } else if (text.contains("바싹") || text.contains("마른 후") || text.contains("단수")) {
            return new int[]{10, 25, 40}; // 다육/선인장류
        }

        return new int[]{30, 45, 65};
    }

    // 광도 텍스트 -> Lux 수치 변환: {최소 Lux, 최대 Lux}
    private int[] mapLightToLux(String text) {
        if (text == null || text.isEmpty()) {
            return new int[]{1000, 3000}; // 기본 실내 간접광
        }

        // 높은 광도 (양지, 베란다 직사광선 유입 구역)
        if (text.contains("높은") || text.contains("1,500~10,000") || text.contains("10,000")) {
            return new int[]{1500, 10000};
        }
        // 낮은 광도 (음지, 실내 안쪽)
        else if (text.contains("낮은") || text.contains("300~1,000") || text.contains("1,000")) {
            return new int[]{300, 1000};
        }
        // 중간 광도 (반음지~반양지, 창가 인근 거실)
        else if (text.contains("중간") || text.contains("1,000~1,500")) {
            return new int[]{1000, 2500};
        }

        return new int[]{1000, 3000};
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

    public List<PlantData> searchPlants(String keyword) {
        return plantDataRepository.findByPlantNameContaining(keyword);
    }
}