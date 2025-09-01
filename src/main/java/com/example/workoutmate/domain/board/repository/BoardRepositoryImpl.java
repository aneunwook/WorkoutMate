package com.example.workoutmate.domain.board.repository;

import com.example.workoutmate.domain.board.dto.BoardResponseDto;
import com.example.workoutmate.domain.board.entity.Board;
import com.example.workoutmate.domain.board.enums.SearchType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.workoutmate.domain.board.entity.QBoard.board;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Board> searchBoards(String keyword, SearchType searchType, Pageable pageable) {

        BooleanBuilder condition = buildSearchCondition(keyword, searchType);

        List<Board> result = queryFactory
                .selectFrom(board)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(board.createdAt.desc())
                .fetch();

        long total = queryFactory
                .select(board.count())
                .from(board)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(result, pageable, total);
    }

    private BooleanBuilder buildSearchCondition(String keyword, SearchType searchType){

        BooleanBuilder condition = new BooleanBuilder().and(board.isDeleted.isFalse());

        if (keyword != null && !keyword.isBlank()){
            switch (searchType) {
                case TITLE -> condition.and(board.title.containsIgnoreCase(keyword));
                case CONTENT -> condition.and(board.content.containsIgnoreCase(keyword));
                case ALL -> condition.and(
                        board.title.containsIgnoreCase(keyword)
                        .or(board.content.containsIgnoreCase(keyword))
                );
            }
        }
        return condition;
    }
}
