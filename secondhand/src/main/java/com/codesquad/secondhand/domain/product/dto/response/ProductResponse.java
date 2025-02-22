package com.codesquad.secondhand.domain.product.dto.response;

import java.time.LocalDateTime;

import com.codesquad.secondhand.domain.product.entity.Product;
import com.codesquad.secondhand.domain.product.utils.ProductStatus;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProductResponse {

	private Long id;
	private Long writerId;
	private String thumbnailUrl;
	private String name;
	private String region;
	private LocalDateTime createdAt;
	private String status;
	private Long price;
	private Long likeCount;
	private Long chattingCount;

	public static ProductResponse of(Product product, Long reactionCount, Long chattingCount) {
		return ProductResponse.builder()
			.id(product.getId())
			.writerId(product.getMember().getId())
			.thumbnailUrl(product.getThumbnailImage())
			.name(product.getName())
			.region(product.getRegion().getName())
			.createdAt(product.getCreatedAt())
			.status(ProductStatus.fromCode(product.getStatus()).getDescription())
			.price(product.getPrice())
			.likeCount(reactionCount)
			.chattingCount(chattingCount)
			.build();
	}
}
