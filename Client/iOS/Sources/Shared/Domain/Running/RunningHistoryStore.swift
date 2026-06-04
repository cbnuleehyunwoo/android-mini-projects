import Foundation

final class RunningHistoryStore {
    private let defaults: UserDefaults
    private let key = "runpamine.running-records.v1"

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func load() -> [RunningRecord] {
        guard let data = defaults.data(forKey: key) else { return [] }
        return (try? JSONDecoder().decode([RunningRecord].self, from: data)) ?? []
    }

    func save(_ records: [RunningRecord]) {
        guard let data = try? JSONEncoder().encode(records) else { return }
        defaults.set(data, forKey: key)
    }

    func prepend(_ record: RunningRecord) {
        var records = load()
        records.insert(record, at: 0)
        save(records)
    }
}
