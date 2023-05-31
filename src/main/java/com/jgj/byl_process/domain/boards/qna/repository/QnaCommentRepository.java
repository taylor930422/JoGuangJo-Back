package com.jgj.byl_process.domain.boards.qna.repository;

import com.jgj.byl_process.domain.boards.qna.entity.QnaComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaCommentRepository extends JpaRepository<QnaComment,Long> {}
