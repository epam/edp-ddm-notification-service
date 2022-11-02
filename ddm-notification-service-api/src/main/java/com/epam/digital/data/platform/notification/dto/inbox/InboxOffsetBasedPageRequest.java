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
package com.epam.digital.data.platform.notification.dto.inbox;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class InboxOffsetBasedPageRequest implements Pageable {

  private final int limit;
  private final int offset;

  private final Sort sort = Sort.by("createdAt").descending();

  public InboxOffsetBasedPageRequest(int limit, int offset) {
    if (limit < 1) {
      throw new IllegalArgumentException("Limit must not be less than one!");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("Offset index must not be less than zero!");
    }
    this.limit = limit;
    this.offset = offset;
  }

  @Override
  public int getPageNumber() {
    return offset / limit;
  }

  @Override
  public int getPageSize() {
    return limit;
  }

  @Override
  public long getOffset() {
    return offset;
  }

  @Override
  public Sort getSort() {
    return sort;
  }


  @Override
  public Pageable next() {
    return new InboxOffsetBasedPageRequest(getPageSize(), (int) (getOffset() + getPageSize()));
  }

  public Pageable previous() {
    return hasPrevious() ?
        new InboxOffsetBasedPageRequest(getPageSize(), (int) (getOffset() - getPageSize())) : this;
  }

  @Override
  public Pageable previousOrFirst() {
    return hasPrevious() ? previous() : first();
  }

  @Override
  public Pageable first() {
    return new InboxOffsetBasedPageRequest(getPageSize(), 0);
  }

  @Override
  public Pageable withPage(int pageNumber) {
    return PageRequest.of(pageNumber, getPageSize(), getSort());
  }

  @Override
  public boolean hasPrevious() {
    return offset > limit;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;

    result = prime * result + limit;
    result = prime * result + offset;

    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    InboxOffsetBasedPageRequest other = (InboxOffsetBasedPageRequest) obj;
    return this.limit == other.limit && this.offset == other.offset;
  }

}