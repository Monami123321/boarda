package site.gongtong.review.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QReview is a Querydsl query type for Review
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReview extends EntityPathBase<Review> {

    private static final long serialVersionUID = -329841631L;

    public static final QReview review = new QReview("review");

    public final StringPath content = createString("content");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Float> rate = createNumber("rate", Float.class);

    public final ComparablePath<Character> status = createComparable("status", Character.class);

    public QReview(String variable) {
        super(Review.class, forVariable(variable));
    }

    public QReview(Path<? extends Review> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReview(PathMetadata metadata) {
        super(Review.class, metadata);
    }

}

