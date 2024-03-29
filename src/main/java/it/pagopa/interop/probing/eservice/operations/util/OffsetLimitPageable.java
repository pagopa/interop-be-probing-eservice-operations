package it.pagopa.interop.probing.eservice.operations.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetLimitPageable implements Pageable {

	private Integer limit;
	private Integer offset;
	private Sort sort;

	public OffsetLimitPageable(int offset, int limit, Sort sort) {
		if (offset < 0)
			throw new IllegalArgumentException("Offset must not be less than zero");

		if (limit <= 0)
			throw new IllegalArgumentException("Limit must be greater than zero");

		this.offset = offset;
		this.limit = limit;

		if (sort != null) {
			this.sort = sort;
		}
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
		return new OffsetLimitPageable((int) getOffset() + getPageSize(), getPageSize(), getSort());
	}

	public OffsetLimitPageable previous() {
		int value = (int) getOffset() - getPageSize();
		value = value >= 0 ? value : 0;
		return hasPrevious() ? new OffsetLimitPageable(value - getPageSize(), getPageSize(), getSort()) : this;
	}

	@Override
	public Pageable previousOrFirst() {
		return hasPrevious() ? previous() : first();
	}

	@Override
	public Pageable first() {
		return new OffsetLimitPageable(0, getPageSize(), getSort());
	}

	@Override
	public Pageable withPage(int pageNumber) {
		return new OffsetLimitPageable(getPageSize() * pageNumber, getPageSize(), getSort());
	}

	@Override
	public boolean hasPrevious() {
		return offset > 0;
	}

}
