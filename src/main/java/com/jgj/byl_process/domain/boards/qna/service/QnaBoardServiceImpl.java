package com.jgj.byl_process.domain.boards.qna.service;

import com.jgj.byl_process.domain.boards.qna.controller.dto.request.QnaBoardRequest;
import com.jgj.byl_process.domain.boards.qna.controller.dto.response.QnaBoardListResponse;
import com.jgj.byl_process.domain.boards.qna.controller.dto.response.QnaBoardModifyResponse;
import com.jgj.byl_process.domain.boards.qna.controller.dto.response.QnaBoardReadResponse;
import com.jgj.byl_process.domain.boards.qna.entity.QnaBoard;
import com.jgj.byl_process.domain.boards.qna.entity.QnaBoardImgResource;
import com.jgj.byl_process.domain.boards.qna.repository.QnaBoardImgRepository;
import com.jgj.byl_process.domain.boards.qna.repository.QnaBoardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class QnaBoardServiceImpl implements QnaBoardService {
    final private QnaBoardRepository qnaBoardRepository;
    final private QnaBoardImgRepository qnaBoardImgRepository;

    public QnaBoardServiceImpl(QnaBoardRepository qnaBoardRepository,
                               QnaBoardImgRepository qnaBoardImgRepository) {
        this.qnaBoardRepository = qnaBoardRepository;
        this.qnaBoardImgRepository = qnaBoardImgRepository;
    }

    @Override
    public void register(List<MultipartFile> imageFileList, QnaBoardRequest qnaBoardRequest) {
        List<QnaBoardImgResource> qnaBoardImgResourcesList = new ArrayList<>();
        final String uploadPath = "E:/Project/JoGuangJo-Front/src/assets/qnaImgs";

        QnaBoard qnaBoard = new QnaBoard();
        qnaBoard.setTitle(qnaBoardRequest.getTitle());
        qnaBoard.setWriter(qnaBoardRequest.getWriter());

        String content = qnaBoardRequest.getContent();

        content = content.replaceAll("<img[^>]*>", "");
        content = content.replaceAll("<p>", "");
        content = content.replaceAll("</p>", "");

        // base64로 디코딩 하지 않고, 단순히 <img> <p> 태그 제거 후 저장
        qnaBoard.setContent(content);

        for (MultipartFile file : imageFileList) {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String filepath = uploadPath + "/" + filename;

            // 이미지 파일을 디스크에 저장
            try {
                file.transferTo(new File(filepath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            QnaBoardImgResource qnaBoardImgResource = new QnaBoardImgResource();
            qnaBoardImgResource.setQnaBoard(qnaBoard);
            qnaBoardImgResource.setFilePath(filepath);

            qnaBoardImgResourcesList.add(qnaBoardImgResource);
        }

        qnaBoard.setImages(qnaBoardImgResourcesList);
        qnaBoardRepository.save(qnaBoard);
        qnaBoardImgRepository.saveAll(qnaBoardImgResourcesList);
    }

    public List<QnaBoardListResponse> list() {
        Sort sort = Sort.by(Sort.Direction.DESC, "qnaBoardId");
        List<QnaBoard> qnaBoardList = qnaBoardRepository.findAll(sort);
        List<QnaBoardListResponse> qnaBoardResponseList = new ArrayList<>();

        for (QnaBoard qnaBoard : qnaBoardList) {
            qnaBoardResponseList.add(new QnaBoardListResponse(
                    qnaBoard.getQnaBoardId(), qnaBoard.getTitle(),
                    qnaBoard.getWriter(), qnaBoard.getRegDate()
            ));
        }
        return qnaBoardResponseList;
    }

    @Override
    public QnaBoardReadResponse read(Long qnaBoardId) {
        Optional<QnaBoard> maybeQnaBoard = qnaBoardRepository.findById(qnaBoardId);
        List<String> maybeQnaBoardImgList = qnaBoardImgRepository.findImagePathByQnaBoardId(qnaBoardId);

        QnaBoard qnaBoard = maybeQnaBoard.get();
        List<String> imageResourcePaths = new ArrayList<>();

        for (String imgPath : maybeQnaBoardImgList) {
            String fileName = imgPath.substring(imgPath.lastIndexOf("/") + 1);
            imageResourcePaths.add(fileName);
        }

        return new QnaBoardReadResponse(
                qnaBoard.getQnaBoardId(),
                qnaBoard.getTitle(),
                qnaBoard.getWriter(),
                qnaBoard.getContent(),
                imageResourcePaths,
                qnaBoard.getRegDate()
        );
    }

    @Override
    public List<QnaBoardModifyResponse> modify(Long qnaBoardId, List<MultipartFile> imageFileList, QnaBoardRequest qnaBoardRequest) {
        Optional<QnaBoard> optionalQnaBoard = qnaBoardRepository.findById(qnaBoardId);

        if (optionalQnaBoard.isEmpty()) {
            System.out.println("QnaBoard 정보를 찾을 수 없습니다. : " + qnaBoardId);
            return null;
        }

        QnaBoard qnaBoard = optionalQnaBoard.get();
        qnaBoard.setTitle(qnaBoardRequest.getTitle());

        String content = qnaBoardRequest.getContent();

        content = content.replaceAll("<img[^>]*>", ""); // <img> 태그 제거
        content = content.replaceAll("<p>", ""); // <p> 태그 시작 부분 제거
        content = content.replaceAll("</p>", ""); // <p> 태그 종료 부분 제거

        // base64로 디코딩 하지 않고 단순히 <img> <p> 태그 replace 후 저장
        qnaBoard.setContent(content);


        List<QnaBoardImgResource> qnaBoardImgResourcesList = new ArrayList<>();
        final String uploadPath = "E:/Project/JoGuangJo-Front/src/assets/qnaImgs";

        for (MultipartFile file : imageFileList) {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String filepath = uploadPath + "/" + filename;

            // 이미지 파일을 디스크에 저장
            try {
                file.transferTo(new File(filepath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // QnaBoardImgResource 객체 생성
            QnaBoardImgResource qnaBoardImgResource = new QnaBoardImgResource();
            qnaBoardImgResource.setQnaBoard(qnaBoard);
            qnaBoardImgResource.setFilePath(filepath);

            // QnaBoardImgResource 객체를 리스트에 추가
            qnaBoardImgResourcesList.add(qnaBoardImgResource);
        }

        qnaBoard.setImages(qnaBoardImgResourcesList);

        // 이미지 정보 저장
        qnaBoardImgRepository.saveAll(qnaBoardImgResourcesList);

        // 수정된 QnaBoard 정보 저장
        qnaBoardRepository.save(qnaBoard);

        List<QnaBoardModifyResponse> qnaBoardModifyResponseList = new ArrayList<>();
        qnaBoardModifyResponseList.add(new QnaBoardModifyResponse(
                qnaBoard.getQnaBoardId(), qnaBoard.getTitle(),
                qnaBoard.getWriter(), qnaBoard.getContent(),
                qnaBoard.getQnaBoardImgResourcesList().toString(), qnaBoard.getRegDate()
        ));

        return qnaBoardModifyResponseList;
    }

    @Override
    public void remove(Long qnaBoardId) {
        qnaBoardRepository.deleteById(qnaBoardId);
    }
}
