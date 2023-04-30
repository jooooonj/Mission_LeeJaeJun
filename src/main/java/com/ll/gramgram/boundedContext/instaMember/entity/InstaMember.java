package com.ll.gramgram.boundedContext.instaMember.entity;

import com.ll.gramgram.base.baseEntity.BaseEntity;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Getter
public class InstaMember extends InstaMemberBase {

    @Column(unique = true)
    private String username;
    @Setter
    private String gender;

    @OneToOne // 1:1
    @Setter
    private Member member;

    @OneToMany(mappedBy = "fromInstaMember", cascade = {CascadeType.ALL})
    @OrderBy("id desc") // 정렬
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Builder.Default // @Builder 가 있으면 ` = new ArrayList<>();` 가 작동하지 않는다. 그래서 이걸 붙여야 한다.
    private List<LikeablePerson> fromLikeablePeople = new ArrayList<>();

    @OneToMany(mappedBy = "toInstaMember", cascade = {CascadeType.ALL})
    @OrderBy("id desc") // 정렬
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Builder.Default // @Builder 가 있으면 ` = new ArrayList<>();` 가 작동하지 않는다. 그래서 이걸 붙여야 한다.
    private List<LikeablePerson> toLikeablePeople = new ArrayList<>();

    @OneToMany(mappedBy = "instaMember", cascade = {CascadeType.ALL})
    @OrderBy("id desc") // 정렬
    @Builder.Default
    private List<InstaMemberSnapshot> instaMemberSnapshots = new ArrayList<>();


    public void connectedByMember(Member member){
        this.member = member;
    }
    public void disConnected(){
        this.member = null;
    }
    public boolean hasConnected(){
        return member != null;
    }

    public void addFromLikeablePerson(LikeablePerson likeablePerson) {
        fromLikeablePeople.add(0, likeablePerson);
    }

    public void addToLikeablePerson(LikeablePerson likeablePerson) {
        toLikeablePeople.add(0, likeablePerson);
    }

    public void delFromLikeablePerson(LikeablePerson likeablePerson) {
        fromLikeablePeople.remove(likeablePerson);
    }

    public void delToLikeablePerson(LikeablePerson likeablePerson) {
        toLikeablePeople.remove(likeablePerson);
    }

    public String getGenderDisplayName() {
        return switch (gender) {
            case "W" -> "여성";
            default -> "남성";
        };
    }
    public boolean updateGender(String gender) {
        if (gender.equals(this.gender)) return false;

        boolean oldIsNull = this.gender == null;

        String oldGender = this.gender;

        getFromLikeablePeople()
                .forEach(likeablePerson -> {
                    // 내가 좋아하는 사람 불러오기
                    InstaMember toInstaMember = likeablePerson.getToInstaMember();
                    toInstaMember.decreaseLikesCount(oldGender, likeablePerson.getAttractiveTypeCode());
                    toInstaMember.increaseLikesCount(gender, likeablePerson.getAttractiveTypeCode());
                });

        this.gender = gender;

        if (!oldIsNull) saveSnapshot();

        return true;
    }

    public void increaseLikesCount(String gender, int attractiveTypeCode) {
        if (gender.equals("W") && attractiveTypeCode == 1) likesCountByWomanAndAttractiveTypeCode1++;
        if (gender.equals("W") && attractiveTypeCode == 2) likesCountByWomanAndAttractiveTypeCode2++;
        if (gender.equals("W") && attractiveTypeCode == 3) likesCountByWomanAndAttractiveTypeCode3++;
        if (gender.equals("M") && attractiveTypeCode == 1) likesCountByManAndAttractiveTypeCode1++;
        if (gender.equals("M") && attractiveTypeCode == 2) likesCountByManAndAttractiveTypeCode2++;
        if (gender.equals("M") && attractiveTypeCode == 3) likesCountByManAndAttractiveTypeCode3++;
        saveSnapshot();
    }


    public void decreaseLikesCount(String gender, int attractiveTypeCode) {
        if (gender.equals("W") && attractiveTypeCode == 1) likesCountByWomanAndAttractiveTypeCode1--;
        if (gender.equals("W") && attractiveTypeCode == 2) likesCountByWomanAndAttractiveTypeCode2--;
        if (gender.equals("W") && attractiveTypeCode == 3) likesCountByWomanAndAttractiveTypeCode3--;
        if (gender.equals("M") && attractiveTypeCode == 1) likesCountByWomanAndAttractiveTypeCode1--;
        if (gender.equals("M") && attractiveTypeCode == 2) likesCountByWomanAndAttractiveTypeCode2--;
        if (gender.equals("M") && attractiveTypeCode == 3) likesCountByWomanAndAttractiveTypeCode3--;
        saveSnapshot();
    }



    public void saveSnapshot() {
        InstaMemberSnapshot instaMemberSnapshot = InstaMemberSnapshot.builder()
                .instaMember(this)
                .username(username)
                .likesCountByWomanAndAttractiveTypeCode1(likesCountByWomanAndAttractiveTypeCode1)
                .likesCountByWomanAndAttractiveTypeCode2(likesCountByWomanAndAttractiveTypeCode2)
                .likesCountByWomanAndAttractiveTypeCode3(likesCountByWomanAndAttractiveTypeCode3)
                .likesCountByManAndAttractiveTypeCode1(likesCountByManAndAttractiveTypeCode1)
                .likesCountByManAndAttractiveTypeCode2(likesCountByManAndAttractiveTypeCode2)
                .likesCountByManAndAttractiveTypeCode3(likesCountByManAndAttractiveTypeCode3)
                .build();

        instaMemberSnapshots.add(instaMemberSnapshot);
    }
}
