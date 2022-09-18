package twscomms

enum class OrderWrapperStatus {
    PendingSubmit,
    PendingCancel,
    PreSubmitted,
    Submitted,
    ApiCancelled,
    Cancelled,
    Filled,
    Inactive
}