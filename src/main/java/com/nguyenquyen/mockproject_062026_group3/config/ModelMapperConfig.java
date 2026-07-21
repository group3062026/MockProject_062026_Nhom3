package com.nguyenquyen.mockproject_062026_group3.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelmapper() {
        ModelMapper modelMapper = new ModelMapper();


        //Chỉ map những trường không null trong DTO
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        // Bắt buộc ModelMapper phải map chính xác tên biến 100% (STRICT)
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }
}
