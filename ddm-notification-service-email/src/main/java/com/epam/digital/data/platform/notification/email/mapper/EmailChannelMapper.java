/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.notification.email.mapper;

import com.epam.digital.data.platform.notification.dto.ChannelObject;
import com.epam.digital.data.platform.notification.mapper.ChannelMapper;
import com.epam.digital.data.platform.settings.model.dto.Channel;
import com.epam.digital.data.platform.settings.model.dto.ChannelReadDto;
import org.springframework.stereotype.Component;

@Component
public class EmailChannelMapper implements ChannelMapper {

  @Override
  public ChannelObject map(ChannelReadDto channelReadDto) {
    return ChannelObject.builder()
        .email(channelReadDto.getAddress())
        .channel(Channel.EMAIL.getValue())
        .build();
  }

  @Override
  public Channel getChannel() {
    return Channel.EMAIL;
  }
}
