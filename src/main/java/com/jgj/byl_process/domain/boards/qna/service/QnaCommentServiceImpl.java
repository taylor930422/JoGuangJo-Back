package com.jgj.byl_process.domain.boards.qna.service;

import com.jgj.byl_process.domain.boards.qna.controller.dto.request.QnaCommentRequest;
import com.jgj.byl_process.domain.boards.qna.controller.dto.response.QnaCommentListResponse;
import com.jgj.byl_process.domain.boards.qna.entity.QnaBoard;
import com.jgj.byl_process.domain.boards.qna.entity.QnaComment;
import com.jgj.byl_process.domain.boards.qna.repository.QnaBoardRepository;
import com.jgj.byl_process.domain.boards.qna.repository.QnaCommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class QnaCommentServiceImpl implements QnaCommentService {

    final private QnaCommentRepository qnaCommentRepository;

    final private QnaBoardRepository qnaBoardRepository;

    public QnaCommentServiceImpl(QnaCommentRepository qnaCommentRepository,
                                 QnaBoardRepository qnaBoardRepository) {
        this.qnaCommentRepository = qnaCommentRepository;
        this.qnaBoardRepository = qnaBoardRepository;
    }

    @Override
    public List<QnaCommentListResponse> qnaCommentList(Long qnaBoardId) {
        List<QnaComment> QnaCommentList = qnaCommentRepository.findAll();
        List<QnaCommentListResponse> QnaCommentResponseList = new ArrayList<>();

        for (QnaComment QnaComment : QnaCommentList) {
            if (QnaComment.getQnaBoard().getQnaBoardId().equals(qnaBoardId)) {
                QnaCommentResponseList.add(new QnaCommentListResponse(
                        QnaComment.getQnaCommentId(), QnaComment.getQnaBoard().getQnaBoardId(),
                        QnaComment.getComment(), QnaComment.getWriter()
                ));
            }
        }
        return QnaCommentResponseList;
    }

    @Override
    public void qnaCommentRegister(QnaCommentRequest commentRequest) {
        Optional<QnaBoard> maybeQnaBoard = qnaBoardRepository.findById(commentRequest.getQnaBoardId());

        QnaComment qnaComment = new QnaComment();
        qnaComment.setQnaBoard(maybeQnaBoard.get());
        qnaComment.setWriter(commentRequest.getWriter());
        qnaComment.setComment(commentRequest.getComment());

        qnaCommentRepository.save(qnaComment);
    }

    @Override
    public QnaComment modify(Long qnaCommentId, QnaCommentRequest qnaCommentRequest) {
        QnaComment qnaComment = qnaCommentRepository.findById(qnaCommentId)
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(qnaCommentId)));

        qnaComment.update(qnaCommentRequest.getComment());

        qnaCommentRepository.save(qnaComment);
        return qnaComment;
    }

    @Override
    public void remove(Long qnaCommentId) {

        qnaCommentRepository.deleteById(qnaCommentId);
    }
}
