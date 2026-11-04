from dataclasses import dataclass


@dataclass(frozen=True)
class Record:
    target: str
    ttl_seconds: int


class Resolver:
    def __init__(self):
        self.cache = {}

    def resolve(self, records, app, now):
        target, expires = self.cache.get(app, (None, 0))
        if now >= expires:
            record = records[app]
            target = record.target
            self.cache[app] = (target, now + record.ttl_seconds)
        return target


def can_use_shared_database(target, available_contracts):
    required_contract = "v1" if target == "old" else "v2"
    return required_contract in available_contracts


def check(condition, label):
    if not condition:
        raise AssertionError(label)
    print(label)


def main():
    apps = ("orders", "inventory", "billing")
    records = {app: Record("old", 300) for app in apps}
    early = Resolver()
    check(early.resolve(records, "orders", 0) == "old",
          "t=0: first resolver cached the old route for 300s")

    records = {app: Record("old", 30) for app in apps}
    records["orders"] = Record("new", 30)
    fresh = Resolver()
    old = early.resolve(records, "orders", 50)
    new = fresh.resolve(records, "orders", 50)
    check((old, new) == ("old", "new"),
          "t=50: early resolver uses old; fresh resolver uses new")
    check(fresh.resolve(records, "inventory", 50) == "old",
          "t=50: inventory remains on old during the staged cutover")

    expanded = {"v1", "v2"}
    check(all(can_use_shared_database(route, expanded) for route in (old, new)),
          "shared DB with v1 and v2 accepts both generations")
    check(not can_use_shared_database(old, {"v2"}),
          "shared DB with v2 only rejects the old app")

    records["orders"] = Record("old", 30)
    check((fresh.resolve(records, "orders", 60),
           Resolver().resolve(records, "orders", 60)) == ("new", "old"),
          "t=60: rollback changes fresh lookups; cached new routes remain")
    check(fresh.resolve(records, "orders", 80) == "old",
          "t=80: the cached new route expired in this model")


if __name__ == "__main__":
    main()
