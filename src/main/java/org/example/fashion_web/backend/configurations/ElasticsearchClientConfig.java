//package org.example.fashion_web.backend.configurations;
//
//import org.apache.http.HttpHeaders;
//import org.elasticsearch.client.RestClientBuilder;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.elasticsearch.client.ClientConfiguration;
//import org.springframework.data.elasticsearch.client.RestClients;
//
//@Configuration
//public class ElasticsearchClientConfig {
//
//    private final String elasticsearchUrl = "https://my-elasticsearch-project-ada03b.es.us-east-1.aws.elastic.cloud:443";
//    private final String apiKey = "ZzBxVk9KWUJjRnROdnB1cTNMelk6SFRGSHM2aEVsNGxNTGJEdXpZYUJBQQ=="; // Thay thế bằng API Key thực tế của bạn
//
//    @Bean
//    public RestHighLevelClient client() {
//        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
//                .connectedTo(elasticsearchUrl)
//                .withClientConfigurer(RestClientBuilder -> {
//                    RestClientBuilder.setDefaultHeaders(new org.apache.http.Header[]{
//                            new org.apache.http.message.BasicHeader("Authorization", "ApiKey " + apiKey)
//                    });
//                    return RestClientBuilder;
//                })
//                .build();
//
//        return RestClients.create(clientConfiguration).rest();
//    }
//}