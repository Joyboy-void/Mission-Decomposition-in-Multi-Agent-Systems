#include "ltlf_external_spot_SpotNativeBridge.h"

#include <spot/tl/parse.hh>
#include <spot/tl/formula.hh>
#include <spot/tl/ltlf.hh>
#include <spot/twaalgos/contains.hh>

#include <sstream>
#include <string>

namespace
{
    std::string toString(JNIEnv* env, jstring value) {
        const char* chars = env->GetStringUTFChars(value, nullptr);

        if(chars == nullptr) {
            return {};
        }

        std::string result(chars);

        env->ReleaseStringUTFChars(value, chars);

        return result;
    }

    void throwIllegalArgument(
            JNIEnv* env,
            const std::string& message) {

        jclass exceptionClass =
                env->FindClass("java/lang/IllegalArgumentException");

        if(exceptionClass != nullptr) {
            env->ThrowNew(exceptionClass, message.c_str());

            env->DeleteLocalRef(exceptionClass);
        }
    }

    jobject makeConstant(
            JNIEnv* env,
            const char* className) {

        jclass clazz = env->FindClass(className);

        if(clazz == nullptr)
            return nullptr;

        jmethodID constructor =
                env->GetMethodID(
                    clazz,
                    "<init>",
                    "()V" );

        if(constructor == nullptr) {
            env->DeleteLocalRef(clazz);
            return nullptr;
        }

        jobject result =
                env->NewObject(clazz, constructor);

        env->DeleteLocalRef(clazz);

        return result;
    }

    jobject makeAtomicProposition(
            JNIEnv* env,
            const std::string& name) {

        jclass clazz =
                env->FindClass(
                        "ltlf/ast/AtomicProposition");

        if(clazz == nullptr)
            return nullptr;

        jmethodID constructor =
                env->GetMethodID(
                        clazz,
                        "<init>",
                        "(Ljava/lang/String;)V");

        if(constructor == nullptr){
            env->DeleteLocalRef(clazz);
            return nullptr;
        }

        jstring javaName =
                env->NewStringUTF(name.c_str());

        jobject result =
                env->NewObject(
                        clazz,
                        constructor,
                        javaName);

        env->DeleteLocalRef(javaName);
        env->DeleteLocalRef(clazz);

        return result;
    }

    jobject makeUnary(
            JNIEnv* env,
            const char* className,
            jobject operand) {

        jclass clazz =
                env->FindClass(className);

        if(clazz == nullptr)
            return nullptr;

        jmethodID constructor =
                env->GetMethodID(
                        clazz,
                        "<init>",
                        "(Lltlf/ast/Formula;)V");

        if(constructor == nullptr) {
            env->DeleteLocalRef(clazz);
            return nullptr;
        }

        jobject result =
                env->NewObject(
                        clazz,
                        constructor,
                        operand);

        env->DeleteLocalRef(clazz);

        return result;
    }

    jobject makeBinary(
            JNIEnv* env,
            const char* className,
            jobject left,
            jobject right) {

        jclass clazz =
                env->FindClass(className);

        if(clazz == nullptr)
            return nullptr;

        jmethodID constructor =
                env->GetMethodID(
                        clazz,
                        "<init>",
                        "(Lltlf/ast/Formula;Lltlf/ast/Formula;)V");

        if(constructor == nullptr) {
            env->DeleteLocalRef(clazz);
            return nullptr;
        }

        jobject result =
                env->NewObject(
                        clazz,
                        constructor,
                        left,
                        right);

        env->DeleteLocalRef(clazz);

        return result;
    }


    // actual recursive formula converter

    jobject toJavaFormula(
            JNIEnv* env,
            const spot::formula& formula) {

        switch (formula.kind()) {

            case spot::op::tt:
                return makeConstant(
                        env,
                        "ltlf/ast/TrueFormula");

            case spot::op::ff:
                return makeConstant(
                        env,
                        "ltlf/ast/FalseFormula");

            case spot::op::ap:
                return makeAtomicProposition(
                        env,
                        formula.ap_name());

            case spot::op::Not: {

                jobject child =
                        toJavaFormula(env, formula[0]);

                if(child == nullptr)
                    return nullptr;

                jobject result =
                        makeUnary(
                                env,
                                "ltlf/ast/Not",
                                child);

                env->DeleteLocalRef(child);

                return result;
            }

            case spot::op::X: {
                jobject child =
                        toJavaFormula(env, formula[0]);

                if(child == nullptr)
                    return nullptr;

                jobject result =
                        makeUnary(
                                env,
                                "ltlf/ast/Next",
                                child);

                env->DeleteLocalRef(child);

                return result;
            }

            case spot::op::F: {

                jobject child =
                        toJavaFormula(env, formula[0]);

                if(child == nullptr)
                    return nullptr;

                jobject result =
                        makeUnary(
                                env,
                                "ltlf/ast/Eventually",
                                child);

                env->DeleteLocalRef(child);

                return result;
            }

            case spot::op::G: {
                jobject child =
                        toJavaFormula(env, formula[0]);

                if(child == nullptr)
                    return nullptr;

                jobject result =
                        makeUnary(
                                env,
                                "ltlf/ast/Always",
                                child);

                env->DeleteLocalRef(child);

                return result;
            }

            // spots formula represents v, ^ as n-ary operators
            case spot::op::And:
            case spot::op::Or:
            {
                if(formula.size() < 2) {
                    throwIllegalArgument(
                            env,
                            "Invalid Boolean operator.");

                    return nullptr;
                }

                jobject result =
                        toJavaFormula(
                                env,
                                formula[0]);

                if(result == nullptr)
                    return nullptr;

                const char* className =
                        formula.kind() == spot::op::And
                        ? "ltlf/ast/And"
                        : "ltlf/ast/Or";

                for (unsigned i = 1; i < formula.size(); i++) {

                    jobject right =
                            toJavaFormula(
                                    env,
                                    formula[i]);

                    if(right == nullptr){
                        env->DeleteLocalRef(result);
                        return nullptr;
                    }

                    jobject combined =
                            makeBinary(
                                    env,
                                    className,
                                    result,
                                    right);

                    env->DeleteLocalRef(result);
                    env->DeleteLocalRef(right);

                    result = combined;

                    if(result == nullptr)
                        return nullptr;
                }

                return result;
            }

            case spot::op::U: {

                jobject left =
                        toJavaFormula(
                                env,
                                formula[0]);

                jobject right =
                        toJavaFormula(
                                env,
                                formula[1]);

                if(left == nullptr || right == nullptr) {
                    if(left)
                        env->DeleteLocalRef(left);

                    if(right)
                        env->DeleteLocalRef(right);

                    return nullptr;
                }

                jobject result =
                        makeBinary(
                                env,
                                "ltlf/ast/Until",
                                left,
                                right);

                env->DeleteLocalRef(left);
                env->DeleteLocalRef(right);

                return result;
            }

            default:
            {
                std::ostringstream message;

                message
                        << "Unsupported operator in LTLf2RA: "
                        << formula;

                throwIllegalArgument(
                        env,
                        message.str());

                return nullptr;
            }
        }
    }
}

extern "C"

JNIEXPORT jboolean JNICALL
Java_ltlf_external_spot_SpotNativeBridge_equivalent(
        JNIEnv* env,
        jclass,
        jstring left,
        jstring right) {

    if(left == nullptr || right == nullptr){
        throwIllegalArgument(
                env,
                "LTLf formulas must not be null.");

        return JNI_FALSE;
    }

    const std::string leftText = toString(env, left);
    const std::string rightText = toString(env, right);

    // Parse left formula.
    spot::parsed_formula leftParsed =
            spot::parse_infix_psl(leftText);

    std::ostringstream leftErrors;

    if(leftParsed.format_errors(leftErrors)
        || leftParsed.f == nullptr){
        throwIllegalArgument(
                env,
                "Invalid left formula:\n"
                + leftErrors.str());

        return JNI_FALSE;
    }

    // Parse right formula.
    spot::parsed_formula rightParsed =
            spot::parse_infix_psl(rightText);

    std::ostringstream rightErrors;

    if(rightParsed.format_errors(rightErrors)
        || rightParsed.f == nullptr){
        throwIllegalArgument(
                env,
                "Invalid right formula:\n"
                + rightErrors.str());

        return JNI_FALSE;
    }

    // parse_infix_psl() accepts PSL syntax too.
    // Our bridge is intended only for LTLf.
    if(!leftParsed.f.is_ltl_formula()){
        throwIllegalArgument(
                env,
                "Left formula is not an LTL formula.");

        return JNI_FALSE;
    }

    if(!rightParsed.f.is_ltl_formula()){
        throwIllegalArgument(
                env,
                "Right formula is not an LTL formula.");

        return JNI_FALSE;
    }


    // Convert LTLf semantics into an LTL formula
    // understood by Spot's omega-automata algorithms.
    const spot::formula leftLtlf =
            spot::from_ltlf(leftParsed.f);

    const spot::formula rightLtlf =
            spot::from_ltlf(rightParsed.f);

    const bool equivalent =
            spot::are_equivalent(leftLtlf, rightLtlf);

    return equivalent ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jobject JNICALL
Java_ltlf_external_spot_SpotNativeBridge_parse(
        JNIEnv* env,
        jclass,
        jstring input)
{
    if(input == nullptr){
        throwIllegalArgument(
                env,
                "Formula must not be null.");

        return nullptr;
    }

    std::string text = toString(env, input);

    spot::parsed_formula parsed =
            spot::parse_infix_psl(text);

    std::ostringstream errors;

    if(parsed.format_errors(errors) || parsed.f == nullptr) {
        throwIllegalArgument(
                env,
                "Invalid LTLf formula:\n"
                + errors.str());

        return nullptr;
    }

    /*
     * parse_infix_psl() accepts PSL too.
     * project supports only the LTLf AST subset.
     */
    if(!parsed.f.is_ltl_formula()) {
        throwIllegalArgument(
                env,
                "Formula contains operators outside LTL.");

        return nullptr;
    }

    return toJavaFormula(
            env,
            parsed.f);
}